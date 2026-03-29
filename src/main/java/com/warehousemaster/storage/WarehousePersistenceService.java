package com.warehousemaster.storage;

import com.warehousemaster.exception.PersistenceException;
import com.warehousemaster.service.Warehouse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WarehousePersistenceService {

    private final ProductStorageStrategy storageStrategy;
    private final Path storagePath;

    public WarehousePersistenceService(ProductStorageStrategy storageStrategy, Path storagePath) {
        this.storageStrategy = storageStrategy;
        this.storagePath = storagePath;
    }

    public void save(Warehouse warehouse) {
        try {
            storageStrategy.save(warehouse.getAllProducts(), storagePath);
        } catch (IOException exception) {
            throw new PersistenceException("Не удалось сохранить данные склада.", exception);
        }
    }

    public void loadInto(Warehouse warehouse) {
        try {
            warehouse.replaceAll(storageStrategy.load(storagePath));
        } catch (IOException exception) {
            throw new PersistenceException("Не удалось загрузить данные склада.", exception);
        }
    }

    public boolean storageExists() {
        return Files.exists(storagePath);
    }

    public Path getStoragePath() {
        return storagePath;
    }
}
