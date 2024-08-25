package hu.otp.mobil.model;

import java.math.BigDecimal;

public record WebshopIncomeSummaryRecord(String webShopId, BigDecimal cardPaymentsSum, BigDecimal transferPaymentsSum) {
}
