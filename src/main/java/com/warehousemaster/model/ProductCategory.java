package com.warehousemaster.model;

public enum ProductCategory {
    FOOD("Food"),
    ELECTRONICS("Electronics");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
