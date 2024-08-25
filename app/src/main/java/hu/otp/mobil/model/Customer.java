package hu.otp.mobil.model;

import java.util.Objects;

public record Customer(String webShopId, String id, String name, String address) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        return Objects.equals(id, customer.id) && Objects.equals(webShopId, customer.webShopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webShopId, id);
    }
}
