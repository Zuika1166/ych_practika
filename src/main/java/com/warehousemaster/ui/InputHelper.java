package com.warehousemaster.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readNonBlankString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("Строка не должна быть пустой. Попробуйте снова.");
        }
    }

    public long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException exception) {
                System.out.println("Нужно ввести целое число. Попробуйте снова.");
            }
        }
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                System.out.println("Нужно ввести целое число. Попробуйте снова.");
            }
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.printf("Введите число в диапазоне от %d до %d.%n", min, max);
        }
    }

    public BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException exception) {
                System.out.println("Нужно ввести число. Попробуйте снова.");
            }
        }
    }

    public LocalDate readLocalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException exception) {
                System.out.println("Введите дату в формате yyyy-MM-dd.");
            }
        }
    }
}
