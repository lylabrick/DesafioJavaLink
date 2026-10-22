package com.console.atm.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class AmountFormatterTest {

    @Test
    public void usesCommaAndTwoDecimalsWithoutGrouping() {
        assertEquals("99999,99", AmountFormatter.format(new BigDecimal("99999.99")));
    }

    @Test
    public void padsMissingDecimals() {
        assertEquals("1500,00", AmountFormatter.format(new BigDecimal("1500")));
    }
}
