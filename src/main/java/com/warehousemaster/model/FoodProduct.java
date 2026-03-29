package com.warehousemaster.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class FoodProduct extends AbstractProduct {

    private LocalDate expirationDate;

    public FoodProduct(long id, String name, BigDecimal price, int quantity, LocalDate expirationDate) {
        super(id, name, price, quantity, ProductCategory.FOOD);
        setExpirationDate(expirationDate);
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = Objects.requireNonNull(expirationDate, "Срок годности обязателен.");
    }

    @Override
    protected String additionalDisplayInfo() {
        return ", expirationDate=" + expirationDate;
    }
}
