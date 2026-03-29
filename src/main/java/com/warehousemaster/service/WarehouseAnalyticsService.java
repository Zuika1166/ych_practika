package com.warehousemaster.service;

import com.warehousemaster.model.AbstractProduct;
import com.warehousemaster.model.FoodProduct;
import com.warehousemaster.model.ProductCategory;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WarehouseAnalyticsService {

    private final Warehouse warehouse;
    private final Clock clock;

    public WarehouseAnalyticsService(Warehouse warehouse, Clock clock) {
        this.warehouse = warehouse;
        this.clock = clock;
    }

    public BigDecimal calculateTotalInventoryValue() {
        return warehouse.getAllProducts().stream()
                .map(AbstractProduct::getInventoryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int calculateTotalUnits() {
        return warehouse.getAllProducts().stream()
                .mapToInt(AbstractProduct::getQuantity)
                .sum();
    }

    public Map<ProductCategory, BigDecimal> calculateValueByCategory() {
        return warehouse.getAllProducts().stream()
                .collect(Collectors.groupingBy(
                        AbstractProduct::getCategory,
                        () -> new EnumMap<>(ProductCategory.class),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                AbstractProduct::getInventoryValue,
                                BigDecimal::add
                        )
                ));
    }

    public Map<ProductCategory, Long> countByCategory() {
        return warehouse.getAllProducts().stream()
                .collect(Collectors.groupingBy(
                        AbstractProduct::getCategory,
                        () -> new EnumMap<>(ProductCategory.class),
                        Collectors.counting()
                ));
    }

    public Optional<AbstractProduct> findMostExpensiveProduct() {
        return warehouse.getAllProducts().stream()
                .max((first, second) -> first.getPrice().compareTo(second.getPrice()));
    }

    public List<FoodProduct> findProductsExpiringWithin(long days) {
        return warehouse.findFoodProductsExpiringWithin(days, clock);
    }

    public List<AbstractProduct> findLowStockProducts(int threshold) {
        return warehouse.findLowStock(threshold);
    }
}
