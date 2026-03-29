package com.warehousemaster.ui;

import com.warehousemaster.exception.PersistenceException;
import com.warehousemaster.exception.WarehouseException;
import com.warehousemaster.model.AbstractProduct;
import com.warehousemaster.model.ElectronicsProduct;
import com.warehousemaster.model.FoodProduct;
import com.warehousemaster.model.ProductCategory;
import com.warehousemaster.service.Warehouse;
import com.warehousemaster.service.WarehouseAnalyticsService;
import com.warehousemaster.storage.WarehousePersistenceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ConsoleMenu {

    private final Warehouse warehouse;
    private final WarehousePersistenceService persistenceService;
    private final WarehouseAnalyticsService analyticsService;
    private final InputHelper inputHelper;
    private boolean running = true;

    public ConsoleMenu(
            Warehouse warehouse,
            WarehousePersistenceService persistenceService,
            WarehouseAnalyticsService analyticsService,
            InputHelper inputHelper
    ) {
        this.warehouse = warehouse;
        this.persistenceService = persistenceService;
        this.analyticsService = analyticsService;
        this.inputHelper = inputHelper;
    }

    public void run() {
        while (running) {
            printMenu();
            int choice = inputHelper.readIntInRange("Выберите пункт меню: ", 0, 9);
            handleChoice(choice);
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== Warehouse Master ==========");
        System.out.println("1. Показать все товары");
        System.out.println("2. Добавить продукт питания");
        System.out.println("3. Добавить электронику");
        System.out.println("4. Обновить количество товара");
        System.out.println("5. Удалить товар");
        System.out.println("6. Поиск по названию");
        System.out.println("7. Показать аналитику");
        System.out.println("8. Сохранить данные в CSV");
        System.out.println("9. Загрузить данные из CSV");
        System.out.println("0. Сохранить и выйти");
        System.out.printf("Файл хранения: %s%n", persistenceService.getStoragePath());
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> showAllProducts();
            case 2 -> addFoodProduct();
            case 3 -> addElectronicsProduct();
            case 4 -> updateQuantity();
            case 5 -> removeProduct();
            case 6 -> searchByName();
            case 7 -> showAnalytics();
            case 8 -> saveWarehouse();
            case 9 -> loadWarehouse();
            case 0 -> exit();
            default -> System.out.println("Неизвестная команда.");
        }
    }

    private void showAllProducts() {
        printProducts(warehouse.getAllProducts());
    }

    private void addFoodProduct() {
        System.out.println("Добавление продукта питания");

        long id = inputHelper.readLong("Введите id: ");
        String name = inputHelper.readNonBlankString("Введите название: ");
        BigDecimal price = inputHelper.readBigDecimal("Введите цену: ");
        int quantity = inputHelper.readInt("Введите количество: ");
        LocalDate expirationDate = inputHelper.readLocalDate("Введите срок годности (yyyy-MM-dd): ");

        try {
            warehouse.addProduct(new FoodProduct(id, name, price, quantity, expirationDate));
            System.out.println("Товар добавлен.");
        } catch (WarehouseException | IllegalArgumentException exception) {
            System.out.println("Не удалось добавить товар: " + exception.getMessage());
        }
    }

    private void addElectronicsProduct() {
        System.out.println("Добавление электроники");

        long id = inputHelper.readLong("Введите id: ");
        String name = inputHelper.readNonBlankString("Введите название: ");
        BigDecimal price = inputHelper.readBigDecimal("Введите цену: ");
        int quantity = inputHelper.readInt("Введите количество: ");
        int warrantyMonths = inputHelper.readInt("Введите гарантийный срок в месяцах: ");

        try {
            warehouse.addProduct(new ElectronicsProduct(id, name, price, quantity, warrantyMonths));
            System.out.println("Товар добавлен.");
        } catch (WarehouseException | IllegalArgumentException exception) {
            System.out.println("Не удалось добавить товар: " + exception.getMessage());
        }
    }

    private void updateQuantity() {
        long id = inputHelper.readLong("Введите id товара: ");
        int quantity = inputHelper.readInt("Введите новое количество: ");

        try {
            warehouse.updateQuantity(id, quantity);
            System.out.println("Количество обновлено.");
        } catch (WarehouseException | IllegalArgumentException exception) {
            System.out.println("Не удалось обновить количество: " + exception.getMessage());
        }
    }

    private void removeProduct() {
        long id = inputHelper.readLong("Введите id товара для удаления: ");

        try {
            warehouse.removeProduct(id);
            System.out.println("Товар удален.");
        } catch (WarehouseException exception) {
            System.out.println("Не удалось удалить товар: " + exception.getMessage());
        }
    }

    private void searchByName() {
        String query = inputHelper.readNonBlankString("Введите часть названия: ");
        List<AbstractProduct> result = warehouse.searchByName(query);
        printProducts(result);
    }

    private void showAnalytics() {
        System.out.println();
        System.out.println("Аналитика склада:");
        System.out.println("Общая стоимость остатков: " + analyticsService.calculateTotalInventoryValue());
        System.out.println("Общее количество единиц товара: " + analyticsService.calculateTotalUnits());

        Map<ProductCategory, Long> countByCategory = analyticsService.countByCategory();
        Map<ProductCategory, BigDecimal> valueByCategory = analyticsService.calculateValueByCategory();

        for (ProductCategory category : ProductCategory.values()) {
            long productCount = countByCategory.getOrDefault(category, 0L);
            BigDecimal inventoryValue = valueByCategory.getOrDefault(category, BigDecimal.ZERO);
            System.out.printf(
                    "%s -> товаров: %d, стоимость остатков: %s%n",
                    category.getDisplayName(),
                    productCount,
                    inventoryValue
            );
        }

        analyticsService.findMostExpensiveProduct()
                .ifPresentOrElse(
                        product -> System.out.println("Самый дорогой товар: " + product),
                        () -> System.out.println("Самый дорогой товар: склад пуст.")
                );

        List<FoodProduct> expiringProducts = analyticsService.findProductsExpiringWithin(7);
        System.out.println("Товары, срок годности которых истекает в ближайшие 7 дней: " + expiringProducts.size());
        expiringProducts.forEach(product -> System.out.println(" - " + product));

        List<AbstractProduct> lowStockProducts = analyticsService.findLowStockProducts(5);
        System.out.println("Товары с остатком <= 5: " + lowStockProducts.size());
        lowStockProducts.forEach(product -> System.out.println(" - " + product));
    }

    private void saveWarehouse() {
        try {
            persistenceService.save(warehouse);
            System.out.println("Данные сохранены.");
        } catch (PersistenceException exception) {
            System.out.println("Ошибка сохранения: " + exception.getMessage());
        }
    }

    private void loadWarehouse() {
        try {
            persistenceService.loadInto(warehouse);
            System.out.printf("Данные загружены. Текущих товаров: %d%n", warehouse.size());
        } catch (PersistenceException exception) {
            System.out.println("Ошибка загрузки: " + exception.getMessage());
        }
    }

    private void exit() {
        saveWarehouse();
        running = false;
        System.out.println("Работа завершена.");
    }

    private void printProducts(List<AbstractProduct> products) {
        if (products.isEmpty()) {
            System.out.println("Список товаров пуст.");
            return;
        }

        System.out.println("Товары на складе:");
        products.forEach(product -> System.out.println(" - " + product));
    }
}
