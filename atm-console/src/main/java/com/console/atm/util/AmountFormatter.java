package com.console.atm.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Da formato a los importes que se muestran al usuario: sin separador de miles y con coma decimal. */
public final class AmountFormatter {

    private AmountFormatter() {
    }

    public static String format(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("0.00", symbols).format(amount);
    }
}
