package com.warehousemaster.service;

import com.warehousemaster.exception.DuplicateProductException;
import com.warehousemaster.exception.ProductNotFoundException;
import com.warehousemaster.model.AbstractProduct;
import com.warehousemaster.model.FoodProduct;
import com.warehousemaster.model.ProductCategory;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Warehouse {

    private final List<AbstractProduct> products = new ArrayList<>();
    private final Map<Long, AbstractProduct> productsById = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void addProduct(AbstractProduct product) {
        Objects.requireNonNull(product, "Товар не должен быть null.");

        lock.writeLock().lock();
        try {
            if (productsById.containsKey(product.getId())) {
                throw new DuplicateProductException("Товар с id=%d уже существует.".formatted(product.getId()));
            }
            products.add(product);
            productsById.put(product.getId(), product);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeProduct(long productId) {
        lock.writeLock().lock();
        try {
            AbstractProduct removedProduct = productsById.remove(productId);
            if (removedProduct == null) {
                throw new ProductNotFoundException("Товар с id=%d не найден.".formatted(productId));
            }
            products.removeIf(product -> product.getId() == productId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void updateQuantity(long productId, int newQuantity) {
        lock.writeLock().lock();
        try {
            AbstractProduct product = productsById.get(productId);
            if (product == null) {
                throw new ProductNotFoundException("Товар с id=%d не найден.".formatted(productId));
            }
            product.setQuantity(newQuantity);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<AbstractProduct> findById(long productId) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(productsById.get(productId));
        } finally {
            lock.readLock().unlock();
        }
    }

    public AbstractProduct requireById(long productId) {
        return findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар с id=%d не найден.".formatted(productId)));
    }

    public List<AbstractProduct> getAllProducts() {
        lock.readLock().lock();
        try {
            return List.copyOf(products);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<AbstractProduct> searchByName(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return getAllProducts().stream()
                .filter(product -> normalizedQuery.isEmpty()
                        || product.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .toList();
    }

    public List<AbstractProduct> findByCategory(ProductCategory category) {
        Objects.requireNonNull(category, "Категория обязательна.");
        return getAllProducts().stream()
                .filter(product -> product.getCategory() == category)
                .toList();
    }

    public List<AbstractProduct> findLowStock(int threshold) {
        return getAllProducts().stream()
                .filter(product -> product.getQuantity() <= threshold)
                .toList();
    }

    public List<FoodProduct> findFoodProductsExpiringWithin(long days, Clock clock) {
        LocalDate now = LocalDate.now(clock);
        LocalDate deadline = now.plusDays(days);

        return getAllProducts().stream()
                .filter(FoodProduct.class::isInstance)
                .map(FoodProduct.class::cast)
                .filter(product -> !product.getExpirationDate().isAfter(deadline))
                .toList();
    }

    public void replaceAll(Collection<? extends AbstractProduct> newProducts) {
        Objects.requireNonNull(newProducts, "Коллекция товаров не должна быть null.");

        Map<Long, AbstractProduct> freshIndex = new HashMap<>();
        List<AbstractProduct> freshProducts = new ArrayList<>();

        for (AbstractProduct product : newProducts) {
            Objects.requireNonNull(product, "Товар внутри коллекции не должен быть null.");
            if (freshIndex.putIfAbsent(product.getId(), product) != null) {
                throw new DuplicateProductException("В загружаемых данных найден дубликат id=%d."
                        .formatted(product.getId()));
            }
            freshProducts.add(product);
        }

        lock.writeLock().lock();
        try {
            products.clear();
            products.addAll(freshProducts);
            productsById.clear();
            productsById.putAll(freshIndex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return products.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
