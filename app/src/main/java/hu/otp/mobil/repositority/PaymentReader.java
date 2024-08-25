package hu.otp.mobil.repositority;

import hu.otp.mobil.model.Customer;
import hu.otp.mobil.model.Payment;
import hu.otp.mobil.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class PaymentReader {
    private static final int COLUMNS = 7;
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setHeader(
                    "wsId",
                    "customerId",
                    "paymentMethod",
                    "amount",
                    "accountNumber",
                    "cardNumber",
                    "date"
            )
            .build();

    private final Reader reader;
    private final Map<String, Map<String, Customer>> customers;

    public List<Payment> readPayments() throws IOException {
        return CSV_FORMAT.parse(reader)
                .stream()
                .map(this::readPayment)
                .filter(Objects::nonNull)
                .filter(this::validatePayment)
                .collect(Collectors.toList());
    }

    private boolean validatePayment(Payment payment) {
        if (!customers.getOrDefault(payment.webShopId(), Collections.emptyMap())
                .containsKey(payment.customerId())) {
            log.warn("Customer {} on webshop {} doesn't exist", payment.customerId(), payment.webShopId());
            return false;
        }

        if(StringUtils.isBlank(payment.paymentMethod().number(payment))) {
            log.warn("Invalid payment information for {}", payment);
            return false;
        }

        return true;
    }

    private Payment readPayment(CSVRecord record) {
        if(record.size() != COLUMNS ){
            log.warn("Customer record {} is ignored, due to having the wrong number of columns ({})", record.getRecordNumber(), record.size());
            return null;
        }

        try{
            return new Payment(
                    record.get("wsId"),
                    record.get("customerId"),
                    PaymentMethod.valueOf(StringUtils.strip(record.get("paymentMethod")).toUpperCase()),
                    new BigDecimal(StringUtils.strip(record.get("amount"))),
                    record.get("accountNumber"),
                    record.get("cardNumber"),
                    DateUtils.parseDate(record.get("date"), "yyyy.MM.dd")
            );
        } catch (NumberFormatException e){
            log.warn("Record {} has an invalid amount field", record.getRecordNumber());
        } catch (ParseException|NullPointerException e) {
            log.warn("Record {} has an invalid date", record.getRecordNumber());
            log.warn(e.getMessage(), e);
        }catch (IllegalArgumentException e) {
            log.warn("Record {} has an invalid payment method", record.getRecordNumber());
        }
        return null;
    }
}
