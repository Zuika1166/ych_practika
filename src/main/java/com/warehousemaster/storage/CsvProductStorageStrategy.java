package com.warehousemaster.storage;

import com.warehousemaster.model.AbstractProduct;
import com.warehousemaster.model.ElectronicsProduct;
import com.warehousemaster.model.FoodProduct;
import com.warehousemaster.model.ProductCategory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CsvProductStorageStrategy implements ProductStorageStrategy {

    private static final String HEADER = "type,id,name,price,quantity,expirationDate,warrantyMonths";

    @Override
    public void save(List<AbstractProduct> products, Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();

            for (AbstractProduct product : products) {
                writer.write(toCsvRow(product));
                writer.newLine();
            }
        }
    }

    @Override
    public List<AbstractProduct> load(Path path) throws IOException {
        List<AbstractProduct> products = new ArrayList<>();

        if (!Files.exists(path)) {
            return products;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null) {
                return products;
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                products.add(parseRow(line, lineNumber));
            }
        }

        return products;
    }

    private AbstractProduct parseRow(String line, int lineNumber) throws IOException {
        List<String> columns = splitCsvLine(line);
        if (columns.size() != 7) {
            throw new IOException("Некорректное количество столбцов в строке " + lineNumber + ".");
        }

        try {
            ProductCategory category = ProductCategory.valueOf(columns.get(0));
            long id = Long.parseLong(columns.get(1));
            String name = columns.get(2);
            BigDecimal price = new BigDecimal(columns.get(3));
            int quantity = Integer.parseInt(columns.get(4));

            if (category == ProductCategory.FOOD) {
                LocalDate expirationDate = LocalDate.parse(columns.get(5));
                return new FoodProduct(id, name, price, quantity, expirationDate);
            }

            int warrantyMonths = Integer.parseInt(columns.get(6));
            return new ElectronicsProduct(id, name, price, quantity, warrantyMonths);
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            throw new IOException("Ошибка чтения CSV в строке " + lineNumber + ": " + exception.getMessage(), exception);
        }
    }

    private String toCsvRow(AbstractProduct product) {
        String expirationDate = "";
        String warrantyMonths = "";

        if (product instanceof FoodProduct foodProduct) {
            expirationDate = foodProduct.getExpirationDate().toString();
        }
        if (product instanceof ElectronicsProduct electronicsProduct) {
            warrantyMonths = Integer.toString(electronicsProduct.getWarrantyMonths());
        }

        return String.join(",",
                escape(product.getCategory().name()),
                escape(Long.toString(product.getId())),
                escape(product.getName()),
                escape(product.getPrice().toPlainString()),
                escape(Integer.toString(product.getQuantity())),
                escape(expirationDate),
                escape(warrantyMonths)
        );
    }

    private List<String> splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);

            if (currentChar == '"') {
                if (insideQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (currentChar == ',' && !insideQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }

        result.add(current.toString());
        return result;
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
