package hu.otp.mobil.model;

import java.math.BigDecimal;
import java.util.Date;

public record Payment(String webShopId, String customerId, PaymentMethod paymentMethod, BigDecimal amountInHuf, String accountNumber, String cardNumber, Date paymentDate) {
}
