package hu.otp.mobil.repositority

import hu.otp.mobil.model.Customer
import hu.otp.mobil.model.PaymentMethod
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class PaymentReaderTest extends Specification {

    @Shared
    def csvToRead = """
WS01;A01;card;2199;;4908366099900433;2021.01.01

WS02;A01;card;12000;;377947047949679;2021.01.13
WS02;A01;card;2000;;377947047949679;2021.01.13
WS02;A01;transfer;2000;;377947047949679;2021.01.13
WS02;A01;card;2000;;;2021.01.13
WS01;A04;transfer;2199;1179400820544448;;2021.01.01
WS02;A01;transfer;2199;1179400820544448;;2021.01.01
WS01;A02;transfer;2199;;;2021.01.01
WS01;A02;transfer;2199;1179400820544448;;2021.01.01;asd
WS01;A02;transfer;2199;1179400820544448;;
WS01;A02;transfer;2199;1179400820544448;;2021.01.a65
WS01;A02;transfer;number;1179400820544448;;2021.01.01
WS01;A02;cash;2199;1179400820544448;;2021.01.01
WS01;A02;;2199;1179400820544448;;2021.01.01
;A02;transfer;2199;1179400820544448;;2021.01.01
WS01;;transfer;2199;1179400820544448;;2021.01.01
WS01;A02;;2199;1179400820544448;;2021.01.01
WS01;A02;transfer;;1179400820544448;;2021.01.01
WS01;A02;transfer;2199;1179400820544448;;
WS01;A02;transfer;2199;1179400820544448;;holnap
"""

    @Shared
    def customers = [
            "WS01": [
                    "A01": new Customer("WS01", "A01", "Kovács János", "Bp. 1192 Kosárfonó u. 18"),
                    "A02": new Customer("WS01", "A02", "Kiss István", "Bp. 1149 Vizafogó u. 11")
            ],
            "WS02": [
                    "A01": new Customer("WS02", "A01", "Nagy Ferenc", "Bp. 1191 Toldi u. 7"),
            ]
    ]

    def "readAllPayments"() {
        setup:
        def subject = new PaymentReader(new StringReader(csvToRead), customers)

        when:
        def result = subject.readPayments()

        then:
        noExceptionThrown()
        result.size() == 4
        result.findAll {
            it.webShopId() == "WS01" && it.customerId() == "A01"
        } .size() == 1
        result.findAll {
            it.webShopId() == "WS02" && it.customerId() == "A01"
        } .size() == 3
        result.findAll {
            it.webShopId() == "WS02" && it.customerId() == "A01" && it.paymentMethod() == PaymentMethod.CARD
        } .size() == 2
    }

}
