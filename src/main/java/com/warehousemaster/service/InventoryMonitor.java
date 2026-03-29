package com.warehousemaster.service;

import com.warehousemaster.model.AbstractProduct;
import com.warehousemaster.model.FoodProduct;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class InventoryMonitor implements AutoCloseable {

    private final Warehouse warehouse;
    private final Clock clock;
    private final long intervalSeconds;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public InventoryMonitor(Warehouse warehouse, Clock clock, long intervalSeconds) {
        this.warehouse = warehouse;
        this.clock = clock;
        this.intervalSeconds = intervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "warehouse-monitor");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(this::printNotifications, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        }
    }

    private void printNotifications() {
        try {
            List<FoodProduct> expiringProducts = warehouse.findFoodProductsExpiringWithin(3, clock);
            List<AbstractProduct> lowStockProducts = warehouse.findLowStock(2);

            if (expiringProducts.isEmpty() && lowStockProducts.isEmpty()) {
                return;
            }

            System.out.println();
            System.out.println("[MONITOR] Состояние склада:");

            if (!expiringProducts.isEmpty()) {
                System.out.println("[MONITOR] Товары с близким сроком годности:");
                expiringProducts.forEach(product ->
                        System.out.println("[MONITOR] " + product));
            }

            if (!lowStockProducts.isEmpty()) {
                System.out.println("[MONITOR] Товары с низким остатком:");
                lowStockProducts.forEach(product ->
                        System.out.println("[MONITOR] " + product));
            }
        } catch (RuntimeException exception) {
            System.out.println("[MONITOR] Ошибка мониторинга: " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }
}
