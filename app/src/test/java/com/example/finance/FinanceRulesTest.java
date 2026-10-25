package com.example.finance;

import org.junit.Test;
import static org.junit.Assert.*;

public class FinanceRulesTest {
    @Test public void acceptsValidIncomeAndExpense() {
        assertNull(FinanceRules.validateTransaction("125.50", "Income", "Salary"));
        assertNull(FinanceRules.validateTransaction("40", "Expense", "Food"));
    }

    @Test public void rejectsMissingAndMalformedAmounts() {
        assertEquals("Please enter an amount.", FinanceRules.validateTransaction("  ", "Income", "Salary"));
        assertEquals("Enter a valid numeric amount.", FinanceRules.validateTransaction("abc", "Income", "Salary"));
        assertEquals("Enter a valid numeric amount.", FinanceRules.validateTransaction("1.2.3", "Expense", "Food"));
    }

    @Test public void rejectsZeroNegativeAndNonFiniteAmounts() {
        assertEquals("Amount must be greater than zero.", FinanceRules.validateTransaction("0", "Income", "Salary"));
        assertEquals("Amount must be greater than zero.", FinanceRules.validateTransaction("-2", "Expense", "Food"));
        assertEquals("Amount must be greater than zero.", FinanceRules.validateTransaction("NaN", "Income", "Salary"));
        assertEquals("Amount must be greater than zero.", FinanceRules.validateTransaction("Infinity", "Income", "Salary"));
    }

    @Test public void rejectsMissingTypeOrCategory() {
        assertEquals("Please select Income or Expense.", FinanceRules.validateTransaction("10", "", "Food"));
        assertEquals("Please select a category.", FinanceRules.validateTransaction("10", "Expense", "Select your category"));
    }

    @Test public void parsesNumericAndCurrencyStringAmounts() {
        assertEquals(12.5, FinanceRules.parseAmount(12.5), 0.0001);
        assertEquals(1200.75, FinanceRules.parseAmount("₹1,200.75"), 0.0001);
        assertEquals(0.0, FinanceRules.parseAmount("not a number"), 0.0001);
        assertEquals(0.0, FinanceRules.parseAmount(null), 0.0001);
    }

    @Test public void calculatesBalanceAndClassifiesIt() {
        assertEquals(60.0, FinanceRules.difference(100.0, 40.0), 0.0001);
        assertEquals("surplus", FinanceRules.balanceMessage(100.0, 40.0));
        assertEquals("deficit", FinanceRules.balanceMessage(40.0, 100.0));
        assertEquals("balanced", FinanceRules.balanceMessage(100.0, 100.0));
        assertEquals(0.0, FinanceRules.difference(0.0, 0.0), 0.0001);
    }
}
