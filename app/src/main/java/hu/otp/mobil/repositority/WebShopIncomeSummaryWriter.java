package hu.otp.mobil.repositority;

import hu.otp.mobil.model.WebshopIncomeSummaryRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.Writer;

public class WebShopIncomeSummaryWriter implements AutoCloseable {
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .build();
    private final CSVPrinter csvPrinter;

    public WebShopIncomeSummaryWriter(Writer writer) throws IOException {
        this.csvPrinter = new CSVPrinter(writer, CSV_FORMAT);
    }

    public void writeRecord(WebshopIncomeSummaryRecord record) throws IOException {
        csvPrinter.printRecord(
                record.webShopId(),
                record.cardPaymentsSum(),
                record.transferPaymentsSum()
        );
    }

    @Override
    public void close() throws IOException {
        csvPrinter.close();
    }
}
