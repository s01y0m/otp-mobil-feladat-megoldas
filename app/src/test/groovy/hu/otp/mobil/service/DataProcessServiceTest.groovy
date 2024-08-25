package hu.otp.mobil.service

import hu.otp.mobil.model.Customer
import hu.otp.mobil.model.CustomerTotalSpentRecord
import hu.otp.mobil.model.Payment
import hu.otp.mobil.model.PaymentMethod
import hu.otp.mobil.model.WebshopIncomeSummaryRecord
import org.apache.tools.ant.taskdefs.SetPermissions
import spock.lang.Shared
import spock.lang.Specification

class DataProcessServiceTest extends Specification {

    @Shared
    def subject = new DataProcessService([
            "WS01": [
                    "A01": new Customer("WS01", "A01", "Test01", "test 1"),
                    "A02": new Customer("WS01", "A02", "Test02", "test 2")
            ],
            "WS02": [
                    "A01": new Customer("WS02", "A01", "Test03", "test 3"),
                    "A02": new Customer("WS02", "A02", "Test04", "nem vasarolt"),
            ],
            "WS03": [
                    "A01": new Customer("WS03", "A01", "Test05", "Ez a webshop nem szerzett penzt"),
            ]
    ],
        [
                new Payment("WS01", "A01", PaymentMethod.CARD, new BigDecimal("42"), "", "card", new Date()),
                new Payment("WS01", "A01", PaymentMethod.CARD, new BigDecimal("420"), "", "card", new Date()),
                new Payment("WS01", "A02", PaymentMethod.CARD, new BigDecimal("69"), "", "card", new Date()),
                new Payment("WS02", "A01", PaymentMethod.TRANSFER, new BigDecimal("1337"), "transfer", "", new Date()),
                new Payment("WS02", "A01", PaymentMethod.CARD, new BigDecimal("31337"), "", "card", new Date())
        ]
    )

    def "webshop income"() {
        expect:
        subject.webShopIncomeSummaryRecords == [
                new WebshopIncomeSummaryRecord("WS01", new BigDecimal("531"), BigDecimal.ZERO),
                new WebshopIncomeSummaryRecord("WS02", new BigDecimal("31337"), new BigDecimal("1337"))
        ]
    }

    def "Top spending customers"() {
        expect:
        subject.topSpenders == [
                new CustomerTotalSpentRecord("Test03", "test 3", new BigDecimal("32674")),
                new CustomerTotalSpentRecord("Test01", "test 1", new BigDecimal("462"))
        ]
    }

    def "customer total spent records"() {
        expect:
        subject.customerTotalSpentRecords == [
                new CustomerTotalSpentRecord("Test01", "test 1", new BigDecimal("462")),
                new CustomerTotalSpentRecord("Test02", "test 2", new BigDecimal("69")),
                new CustomerTotalSpentRecord("Test03", "test 3", new BigDecimal("32674"))
        ]

    }
}
