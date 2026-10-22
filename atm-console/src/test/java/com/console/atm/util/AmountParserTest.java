package com.console.atm.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class AmountParserTest {

    @Test
    public void parsesIntegerAndNormalizesToTwoDecimals() {
        assertEquals(new BigDecimal("1500.00"), AmountParser.parse("1500"));
    }

    @Test
    public void acceptsDotAsDecimalSeparator() {
        assertEquals(new BigDecimal("1500.50"), AmountParser.parse("1500.50"));
    }

    @Test
    public void acceptsCommaAsDecimalSeparator() {
        assertEquals(new BigDecimal("1500.50"), AmountParser.parse("1500,5"));
    }

    @Test
    public void acceptsTrailingZerosBeyondTwoDecimals() {
        assertEquals(new BigDecimal("10.50"), AmountParser.parse("10.500"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsZero() {
        AmountParser.parse("0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegative() {
        AmountParser.parse("-5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMoreThanTwoDecimals() {
        AmountParser.parse("10.505");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsText() {
        AmountParser.parse("abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsExponentNotation() {
        AmountParser.parse("1e3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNull() {
        AmountParser.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMoreThanThirteenIntegerDigits() {
        AmountParser.parse("12345678901234");
    }
}
