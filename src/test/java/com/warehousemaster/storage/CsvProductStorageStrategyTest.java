package com.warehousemaster.storage;

import com.warehousemaster.model.AbstractProduct;
import com.warehousemaster.model.ElectronicsProduct;
import com.warehousemaster.model.FoodProduct;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvProductStorageStrategyTest {

    @Test
    void saveAndLoadRoundTripWorks(@TempDir Path tempDir) throws Exception {
        CsvProductStorageStrategy strategy = new CsvProductStorageStrategy();
        Path filePath = tempDir.resolve("warehouse.csv");
        List<AbstractProduct> originalProducts = List.of(
                new FoodProduct(1L, "Cheese, Premium", new BigDecimal("450.00"), 5, LocalDate.of(2026, 4, 5)),
                new ElectronicsProduct(2L, "Laptop", new BigDecimal("98000.00"), 1, 12)
        );

        strategy.save(originalProducts, filePath);
        List<AbstractProduct> loadedProducts = strategy.load(filePath);

        assertEquals(2, loadedProducts.size());
        assertEquals("Cheese, Premium", loadedProducts.get(0).getName());
        assertTrue(loadedProducts.get(0) instanceof FoodProduct);
        assertTrue(loadedProducts.get(1) instanceof ElectronicsProduct);
    }
}
