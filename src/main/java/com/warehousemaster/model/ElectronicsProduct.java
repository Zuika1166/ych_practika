package com.warehousemaster.model;

import java.math.BigDecimal;

public class ElectronicsProduct extends AbstractProduct {

    private int warrantyMonths;

    public ElectronicsProduct(long id, String name, BigDecimal price, int quantity, int warrantyMonths) {
        super(id, name, price, quantity, ProductCategory.ELECTRONICS);
        setWarrantyMonths(warrantyMonths);
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        if (warrantyMonths < 0) {
            throw new IllegalArgumentException("Гарантийный срок не может быть отрицательным.");
        }
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    protected String additionalDisplayInfo() {
        return ", warrantyMonths=" + warrantyMonths;
    }
}
