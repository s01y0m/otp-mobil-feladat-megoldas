package hu.otp.mobil.repositority;

import hu.otp.mobil.model.CustomerTotalSpentRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.Writer;

public class TopSpendersReportWriter implements AutoCloseable {

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .build();
    private final CSVPrinter csvPrinter;
    public TopSpendersReportWriter(Writer writer) throws IOException {
        this.csvPrinter = new CSVPrinter(writer, CSV_FORMAT);
    }

    public void writeEntry(CustomerTotalSpentRecord customerTotalSpentRecord) throws IOException {
        csvPrinter.printRecord(
                customerTotalSpentRecord.name(),
                customerTotalSpentRecord.address(),
                customerTotalSpentRecord.amountSpent()
        );
    }

    @Override
    public void close() throws IOException {
        csvPrinter.close();
    }
}
