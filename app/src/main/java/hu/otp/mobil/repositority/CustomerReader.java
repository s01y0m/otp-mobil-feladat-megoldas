package hu.otp.mobil.repositority;

import hu.otp.mobil.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;

@RequiredArgsConstructor
@Slf4j
public class CustomerReader {
    private static final int COLUMNS = 4;
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setHeader(
                    "wsid",
                    "clientId",
                    "name",
                    "addr"
            )
            .build();

    private final Reader reader;

    public Map<String, Map<String, Customer>> readAllCustomers() throws IOException {
        return CSV_FORMAT.parse(reader)
                .stream()
                .map(this::readCustomer)
                .filter(Objects::nonNull)
                .filter(this::validateCustomer)
                .collect(Collector.of(
                        HashMap::new, //customers by id, grouped by webshop id
                        this::collectCustomer,
                        (aggr1, aggr2) -> {
                            throw new NotImplementedException("Parallel streams aren't supported!");
                        },
                        Collector.Characteristics.IDENTITY_FINISH
                ));
    }

    private void collectCustomer(Map<String, Map<String, Customer>> customersByWebShop, Customer customer) {
        customersByWebShop.compute(
                customer.webShopId(),
                (wsId, customers) -> addCustomerToWebShop(customers, customer)
        );
    }

    private Map<String, Customer> addCustomerToWebShop(Map<String, Customer> customersMap, Customer customer) {
        Map<String, Customer> customers = customersMap == null ? new HashMap<>() : customersMap;

        var savedCustomer = customers.get(customer.id());
        if (savedCustomer != null) {
            log.warn("Ignoring duplicated customer {} in favor of record {}", customer, savedCustomer);
            return customers;
        }

        customers.put(customer.id(), customer);

        return customers;
    }

    private boolean validateCustomer(Customer customer) {
        if (!StringUtils.isAnyBlank(
                customer.webShopId(),
                customer.id(),
                customer.name(),
                customer.address()
        )){
            return true;
        }
        log.warn("Not valid record: {}", customer);
        return false;
    }

    private Customer readCustomer(CSVRecord record) {
        if(record.size() != COLUMNS ){
            log.warn("Customer record {} is ignored, due to having the wrong number of columns ({})", record.getRecordNumber(), record.size());
            return null;
        }
        return new Customer(
                record.get("wsid"),
                record.get("clientId"),
                record.get("name"),
                record.get("addr")
        );
    }
}
