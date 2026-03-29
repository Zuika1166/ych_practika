package com.warehousemaster;

import com.warehousemaster.exception.PersistenceException;
import com.warehousemaster.service.InventoryMonitor;
import com.warehousemaster.service.Warehouse;
import com.warehousemaster.service.WarehouseAnalyticsService;
import com.warehousemaster.storage.CsvProductStorageStrategy;
import com.warehousemaster.storage.ProductStorageStrategy;
import com.warehousemaster.storage.WarehousePersistenceService;
import com.warehousemaster.ui.ConsoleMenu;
import com.warehousemaster.ui.InputHelper;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Scanner;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();
        ProductStorageStrategy storageStrategy = new CsvProductStorageStrategy();
        WarehousePersistenceService persistenceService =
                new WarehousePersistenceService(storageStrategy, Path.of("data", "warehouse.csv"));

        preloadData(warehouse, persistenceService);

        try (Scanner scanner = new Scanner(System.in);
             InventoryMonitor monitor = new InventoryMonitor(warehouse, Clock.systemDefaultZone(), 30)) {
            InputHelper inputHelper = new InputHelper(scanner);
            WarehouseAnalyticsService analyticsService =
                    new WarehouseAnalyticsService(warehouse, Clock.systemDefaultZone());
            ConsoleMenu consoleMenu =
                    new ConsoleMenu(warehouse, persistenceService, analyticsService, inputHelper);

            System.out.println("Warehouse Master запущен.");
            System.out.println("Фоновый монитор проверяет товары каждые 30 секунд.");
            monitor.start();
            consoleMenu.run();
        }
    }

    private static void preloadData(Warehouse warehouse, WarehousePersistenceService persistenceService) {
        if (!persistenceService.storageExists()) {
            System.out.println("Файл данных пока не найден. Будет создан при первом сохранении.");
            return;
        }

        try {
            persistenceService.loadInto(warehouse);
            System.out.printf("Загружено товаров: %d%n", warehouse.size());
        } catch (PersistenceException exception) {
            System.out.println("Не удалось загрузить данные из файла: " + exception.getMessage());
        }
    }
}
