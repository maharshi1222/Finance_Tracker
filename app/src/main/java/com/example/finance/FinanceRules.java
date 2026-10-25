package com.example.finance;

/** Pure Java validation and money helpers shared by UI code and unit tests. */
public final class FinanceRules {
    private FinanceRules() { }

    /** Returns null when valid, otherwise a user-readable validation message. */
    public static String validateTransaction(String amountText, String type, String category) {
        if (amountText == null || amountText.trim().isEmpty()) {
            return "Please enter an amount.";
        }
        final double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException ex) {
            return "Enter a valid numeric amount.";
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            return "Amount must be greater than zero.";
        }
        if (type == null || !(type.equalsIgnoreCase("Income") || type.equalsIgnoreCase("Expense"))) {
            return "Please select Income or Expense.";
        }
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("Select your category")) {
            return "Please select a category.";
        }
        return null;
    }

    /** Safely parses Firestore numeric/string amounts; malformed values count as zero. */
    public static double parseAmount(Object rawAmount) {
        if (rawAmount instanceof Number) {
            double amount = ((Number) rawAmount).doubleValue();
            return Double.isNaN(amount) || Double.isInfinite(amount) ? 0.0 : amount;
        }
        if (rawAmount instanceof String) {
            String value = ((String) rawAmount).trim().replaceAll("[^0-9.\\-]", "");
            if (value.isEmpty()) return 0.0;
            try {
                double amount = Double.parseDouble(value);
                return Double.isNaN(amount) || Double.isInfinite(amount) ? 0.0 : amount;
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public static double difference(double income, double expense) {
        return income - expense;
    }

    public static String balanceMessage(double income, double expense) {
        if (income > expense) return "surplus";
        if (income < expense) return "deficit";
        return "balanced";
    }
}
