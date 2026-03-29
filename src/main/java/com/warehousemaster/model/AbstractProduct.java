package com.warehousemaster.model;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class AbstractProduct {

    private final long id;
    private final ProductCategory category;
    private String name;
    private BigDecimal price;
    private int quantity;

    protected AbstractProduct(long id, String name, BigDecimal price, int quantity, ProductCategory category) {
        if (id <= 0) {
            throw new IllegalArgumentException("Идентификатор товара должен быть положительным.");
        }
        this.id = id;
        this.category = Objects.requireNonNull(category, "Категория товара обязательна.");
        setName(name);
        setPrice(price);
        setQuantity(quantity);
    }

    public long getId() {
        return id;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название товара не должно быть пустым.");
        }
        this.name = name.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена товара должна быть неотрицательной.");
        }
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Количество товара должно быть неотрицательным.");
        }
        this.quantity = quantity;
    }

    public BigDecimal getInventoryValue() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    protected abstract String additionalDisplayInfo();

    @Override
    public String toString() {
        return String.format(
                "[%s] id=%d, name='%s', price=%s, quantity=%d%s",
                category.getDisplayName(),
                id,
                name,
                price.toPlainString(),
                quantity,
                additionalDisplayInfo()
        );
    }
}
