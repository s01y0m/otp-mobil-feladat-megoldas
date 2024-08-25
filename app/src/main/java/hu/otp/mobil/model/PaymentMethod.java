package hu.otp.mobil.model;

import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.function.Function;

@RequiredArgsConstructor
public enum PaymentMethod {
    CARD(Payment::cardNumber),
    TRANSFER(Payment::accountNumber);

    private final Function<Payment, String> numberExtractor;

    public String number(Payment payment) {
        return numberExtractor.apply(payment);
    }
}
