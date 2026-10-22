package com.console.atm.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Convierte el importe ingresado por consola en un BigDecimal valido para el servicio. */
public final class AmountParser {

    /** Maximo de digitos enteros que acepta el servicio (@Digits(integer = 13)). */
    private static final int MAX_INTEGER_DIGITS = 13;
    private static final int MAX_DECIMALS = 2;
    private static final Pattern FORMAT = Pattern.compile("^\\d+([.,]\\d+)?$");

    private AmountParser() {
    }

    /**
     * @param raw importe con "." o "," como separador decimal (por ejemplo "1500", "1500.50", "1500,5")
     * @throws IllegalArgumentException si no es un numero positivo con hasta 2 decimales
     */
    public static BigDecimal parse(String raw) {
        if (raw == null || !FORMAT.matcher(raw.trim()).matches()) {
            throw new IllegalArgumentException("Importe invalido: '" + raw + "'. Use un numero positivo, por ejemplo 1500.50");
        }

        BigDecimal amount = new BigDecimal(raw.trim().replace(',', '.'));

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero");
        }
        BigDecimal normalized = amount.stripTrailingZeros();
        if (normalized.scale() > MAX_DECIMALS) {
            throw new IllegalArgumentException("El importe admite como maximo " + MAX_DECIMALS + " decimales");
        }
        if (normalized.precision() - normalized.scale() > MAX_INTEGER_DIGITS) {
            throw new IllegalArgumentException("El importe es demasiado grande");
        }
        return normalized.setScale(MAX_DECIMALS);
    }
}
