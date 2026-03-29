package com.warehousemaster.service;

import com.warehousemaster.model.ElectronicsProduct;
import com.warehousemaster.model.FoodProduct;
import com.warehousemaster.model.ProductCategory;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarehouseAnalyticsServiceTest {

    @Test
    void analyticsMethodsProduceExpectedResults() {
        Warehouse warehouse = new Warehouse();
        warehouse.addProduct(new FoodProduct(1L, "Milk", new BigDecimal("90.00"), 4, LocalDate.of(2026, 4, 2)));
        warehouse.addProduct(new ElectronicsProduct(2L, "Monitor", new BigDecimal("15000.00"), 2, 24));
        warehouse.addProduct(new FoodProduct(3L, "Yogurt", new BigDecimal("75.00"), 3, LocalDate.of(2026, 3, 31)));
        warehouse.addProduct(new FoodProduct(4L, "Rice", new BigDecimal("120.00"), 7, LocalDate.of(2026, 4, 10)));

        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-30T00:00:00Z"), ZoneId.of("UTC"));
        WarehouseAnalyticsService analyticsService = new WarehouseAnalyticsService(warehouse, fixedClock);

        assertEquals(new BigDecimal("31425.00"), analyticsService.calculateTotalInventoryValue());
        assertEquals(16, analyticsService.calculateTotalUnits());
        assertEquals(Map.of(ProductCategory.FOOD, 3L, ProductCategory.ELECTRONICS, 1L), analyticsService.countByCategory());
        assertEquals(2, analyticsService.findProductsExpiringWithin(3).size());
        assertEquals(2, analyticsService.findLowStockProducts(3).size());
    }
}
