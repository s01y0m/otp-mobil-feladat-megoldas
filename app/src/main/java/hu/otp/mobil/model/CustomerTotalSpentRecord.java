package hu.otp.mobil.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public record CustomerTotalSpentRecord(String name, String address, BigDecimal amountSpent) {
}
