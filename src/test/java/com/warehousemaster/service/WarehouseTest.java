package com.warehousemaster.service;

import com.warehousemaster.exception.DuplicateProductException;
import com.warehousemaster.model.FoodProduct;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarehouseTest {

    @Test
    void addProductStoresAndFindsItById() {
        Warehouse warehouse = new Warehouse();
        FoodProduct apples = new FoodProduct(1L, "Apples", new BigDecimal("120.50"), 10, LocalDate.now().plusDays(5));

        warehouse.addProduct(apples);

        assertEquals(1, warehouse.size());
        assertTrue(warehouse.findById(1L).isPresent());
        assertEquals("Apples", warehouse.requireById(1L).getName());
    }

    @Test
    void duplicateProductIdThrowsException() {
        Warehouse warehouse = new Warehouse();
        warehouse.addProduct(new FoodProduct(1L, "Milk", new BigDecimal("80.00"), 6, LocalDate.now().plusDays(4)));

        assertThrows(
                DuplicateProductException.class,
                () -> warehouse.addProduct(new FoodProduct(1L, "Cheese", new BigDecimal("300.00"), 2, LocalDate.now().plusDays(8)))
        );
    }

    @Test
    void updateQuantityChangesStoredProduct() {
        Warehouse warehouse = new Warehouse();
        warehouse.addProduct(new FoodProduct(3L, "Bread", new BigDecimal("50.00"), 3, LocalDate.now().plusDays(2)));

        warehouse.updateQuantity(3L, 15);

        assertEquals(15, warehouse.requireById(3L).getQuantity());
    }
}
