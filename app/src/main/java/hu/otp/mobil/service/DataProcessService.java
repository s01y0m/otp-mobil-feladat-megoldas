package hu.otp.mobil.service;

import hu.otp.mobil.model.*;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DataProcessService {
    private static final int TOP_SPENDERS_COUNT = 2;
    private final Map<String, Map<String, Customer>> customers;
    private final List<Payment> payments;

    private List<CustomerTotalSpentRecord> customerTotalSpentRecords;
    private List<WebshopIncomeSummaryRecord> webShopIncomeSummaryRecords;
    private List<CustomerTotalSpentRecord> topSpenders;

    private volatile boolean initialized = false;

    private synchronized void init(){
        if (initialized) {
            return;
        }
        collectSpendingLists();
        initialized = true;
    }

    private void collectSpendingLists() {
        //[webshopId:[customerId: spent]]
        Map<String, Map<String, BigDecimal>> customerSpendingMap = new HashMap<>();
        //[webshopId: income]
        Map<String, BigDecimal> webShopCardIncome = new HashMap<>();
        Map<String, BigDecimal> webShopTransferIncome = new HashMap<>();
        Set<String> allWebshopIds = new HashSet<>();

        for (var payment:payments) {
            customerSpendingMap.computeIfAbsent(payment.webShopId(), s -> new HashMap<>())
                    .compute(payment.customerId(), (k, val) ->
                            val == null ? payment.amountInHuf() : val.add(payment.amountInHuf())
                    );

            allWebshopIds.add(payment.webShopId());
            Map<String, BigDecimal> webshopIncomeMapForPaymentMethod = (payment.paymentMethod() == PaymentMethod.CARD ?
                    webShopCardIncome :
                    webShopTransferIncome
            );

            webshopIncomeMapForPaymentMethod.compute(
                    payment.webShopId(),
                    (s, value) ->
                        value == null ? payment.amountInHuf() : value.add(payment.amountInHuf())
                );
        }

        //In case of only working with small dataset, async is overkill, but will do wonders on larger ones
        var webShopIncomeCollectTask = CompletableFuture.runAsync(() -> collectWebShopIncomeSummaryRecords(webShopCardIncome, webShopTransferIncome, allWebshopIds));
        collectCustomerTotalSpentRecords(customerSpendingMap);
        findTopSpenders();
        webShopIncomeCollectTask.join();
    }

    private void findTopSpenders() {
        topSpenders = customerTotalSpentRecords.stream()
                .sorted(Comparator.comparing(CustomerTotalSpentRecord::amountSpent).reversed())
                .limit(TOP_SPENDERS_COUNT)
                .toList();
    }

    private void collectCustomerTotalSpentRecords(Map<String, Map<String, BigDecimal>> customerSpendingMap){
        customerTotalSpentRecords = customerSpendingMap.entrySet().stream()
                .flatMap(wsIdToCustomerTotalEntry ->
                        wsIdToCustomerTotalEntry
                                .getValue()
                                .entrySet()
                                .stream()
                                .map(customerIdToSpendingEntry -> {
                                    var customer = customers.get(wsIdToCustomerTotalEntry.getKey())
                                            .get(customerIdToSpendingEntry.getKey());
                                    return new CustomerTotalSpentRecord(
                                            customer.name(),
                                            customer.address(),
                                            customerIdToSpendingEntry.getValue()
                                    );
                                })
                )
                .toList();
    }

    private void collectWebShopIncomeSummaryRecords(Map<String, BigDecimal> webShopCardIncome, Map<String, BigDecimal> webShopTransferIncome, Set<String> allWebshopIds){
        webShopIncomeSummaryRecords = allWebshopIds.stream().map(
                s ->
                        new WebshopIncomeSummaryRecord(
                                s,
                                webShopCardIncome.getOrDefault(s, BigDecimal.ZERO),
                                webShopTransferIncome.getOrDefault(s, BigDecimal.ZERO)
                        )
                ).toList();
    }

    private void ensureInitialized(){
        if(!initialized) {
            init();
        }
    }

    public List<CustomerTotalSpentRecord> getCustomerTotalSpentRecords() {
        ensureInitialized();
        return customerTotalSpentRecords;
    }

    public List<WebshopIncomeSummaryRecord> getWebShopIncomeSummaryRecords() {
        ensureInitialized();
        return webShopIncomeSummaryRecords;
    }

    public List<CustomerTotalSpentRecord> getTopSpenders() {
        ensureInitialized();
        return topSpenders;
    }
}
