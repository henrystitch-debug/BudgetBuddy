package com.github.budgetbuddy.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Locale;

public class MoneyUtilsTest {

    @Test
    public void toCents_validInput_returnsCents() {
        assertEquals(1200L, MoneyUtils.toCents("12.00", Locale.US));
        assertEquals(1299L, MoneyUtils.toCents("12.99", Locale.US));
        // Truncate to 2 decimal places before conversion to Cents
        assertEquals(1299L, MoneyUtils.toCents("12.999", Locale.US));
        assertEquals(1200L, MoneyUtils.toCents("12", Locale.US));
    }

    @Test
    public void toCents_invalidInput_returnsZero() {
        assertEquals(0L, MoneyUtils.toCents(null, Locale.US));
        assertEquals(0L, MoneyUtils.toCents("", Locale.US));
        assertEquals(0L, MoneyUtils.toCents("abc", Locale.US));
    }

    @Test
    public void fromCentsRaw_returnsFormattedString() {
        assertEquals("12.99", MoneyUtils.fromCentsRaw(1299L));
        assertEquals("0.05", MoneyUtils.fromCentsRaw(5L));
        assertEquals("12.00", MoneyUtils.fromCentsRaw(1200L));
    }

    @Test
    public void isValidMoneyInput_validatesCorrectly() {
        assertTrue(MoneyUtils.isValidMoneyInput("12"));
        assertTrue(MoneyUtils.isValidMoneyInput("12.9"));
        assertTrue(MoneyUtils.isValidMoneyInput("12.99"));
        
        assertFalse(MoneyUtils.isValidMoneyInput(null));
        assertFalse(MoneyUtils.isValidMoneyInput(""));
        assertFalse(MoneyUtils.isValidMoneyInput("abc"));
        // Based on regex TWO_DEC_PLACES_REGEX, this is not a valid input
        assertFalse(MoneyUtils.isValidMoneyInput("12.999"));
    }

    @Test
    public void toCentsOrNull_returnsNullOnInvalid() {
        assertNull(MoneyUtils.toCentsOrNull("abc"));
        assertNull(MoneyUtils.toCentsOrNull(null));
        assertEquals(Long.valueOf(1299L), MoneyUtils.toCentsOrNull("12.99"));
    }
}
