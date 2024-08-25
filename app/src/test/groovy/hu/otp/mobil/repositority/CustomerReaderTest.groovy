package hu.otp.mobil.repositority

import hu.otp.mobil.model.Customer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class CustomerReaderTest extends Specification {

    @Shared
    def happyCase = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
WS01;A02;Kiss István;Bp. 1149 Vizafogó u. 11
"""
    @Shared
    def emptyLine = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18

WS01;A02;Kiss István;Bp. 1149 Vizafogó u. 11
"""
    @Shared
    def duplicatedId = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
WS01;A02;Kiss István;Bp. 1149 Vizafogó u. 11
WS01;A02;Pézsma Réka;Vecsés 2221 Káposzta u. 22
"""
    @Shared
    def exp1 = [
        "WS01": [
            "A01": new Customer("WS01", "A01", "Kovács János", "Bp. 1192 Kosárfonó u. 18"),
            "A02": new Customer("WS01", "A02", "Kiss István", "Bp. 1149 Vizafogó u. 11")
        ]
    ]

    @Shared
    def missingWsid = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
;A02;Kiss István;Bp. 1149 Vizafogó u. 11
"""
    @Shared
    def missingId = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
WS01;;Kiss István;Bp. 1149 Vizafogó u. 11
"""
    @Shared
    def missingName = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
WS01;A02;;Bp. 1149 Vizafogó u. 11
"""
    @Shared
    def missingAddress = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
WS01;A02;Kiss István;
"""
    @Shared
    def lessColumn = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
WS01;A02;Kiss István
"""
    @Shared
    def moreColumn = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
WS01;A02;Kiss István;Bp. 1149 Vizafogó u. 11;new data
"""

    @Shared
    def exp2 = [
        "WS01": [
            "A01": new Customer("WS01", "A01", "Kovács János", "Bp. 1192 Kosárfonó u. 18"),
        ]
    ]

    @Shared
    def moreWebshop = """
WS01;A01;Kovács János;Bp. 1192 Kosárfonó u. 18
WS01;A02;Kiss István;Bp. 1149 Vizafogó u. 11
WS02;A01;Nagy Ferenc;Bp. 1191 Toldi u. 7
"""

    @Shared
    def exp3 = [
        "WS01": [
            "A01": new Customer("WS01", "A01", "Kovács János", "Bp. 1192 Kosárfonó u. 18"),
            "A02": new Customer("WS01", "A02", "Kiss István", "Bp. 1149 Vizafogó u. 11")
        ],
        "WS02": [
            "A01": new Customer("WS02", "A01", "Nagy Ferenc", "Bp. 1191 Toldi u. 7"),
        ]
    ]

    @Unroll
    def "readAllCustomers #testCase"() {
        setup:
        def subject = new CustomerReader(new StringReader(csvContent))

        when:
        def result = subject.readAllCustomers()

        then:
        noExceptionThrown()
        result == expected

        where:
        testCase            | csvContent     | expected
        "happy case"        | happyCase      | exp1
        "empty line"        | emptyLine      | exp1
        "duplicated id"     | duplicatedId   | exp1
        "empty webshop id"  | missingWsid    | exp2
        "empty id"          | missingId      | exp2
        "empty name"        | missingName    | exp2
        "empty address"     | missingAddress | exp2
        "less columns"      | lessColumn     | exp2
        "more columns"      | moreColumn     | exp2
        "more webshop"      | moreWebshop    | exp3
    }

}
