package com.servicio.atm.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

    private Account cuentaConSaldo(String saldo) {
        return new Account(1L, "0001", "0000000000000000000001", new BigDecimal(saldo));
    }

    @Test
    @DisplayName("acreditar suma el importe al saldo")
    void acreditarSumaElImporte() {
        Account cuenta = cuentaConSaldo("100.00");

        cuenta.acreditar(new BigDecimal("50.25"));

        assertEquals(0, new BigDecimal("150.25").compareTo(cuenta.getSaldo()));
    }

    @Test
    @DisplayName("acreditar rechaza un importe nulo")
    void acreditarRechazaImporteNulo() {
        Account cuenta = cuentaConSaldo("100.00");

        assertThrows(IllegalArgumentException.class, () -> cuenta.acreditar(null));
        assertEquals(0, new BigDecimal("100.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    @DisplayName("acreditar rechaza un importe en cero")
    void acreditarRechazaImporteCero() {
        Account cuenta = cuentaConSaldo("100.00");

        assertThrows(IllegalArgumentException.class, () -> cuenta.acreditar(BigDecimal.ZERO));
        assertEquals(0, new BigDecimal("100.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    @DisplayName("acreditar rechaza un importe negativo")
    void acreditarRechazaImporteNegativo() {
        Account cuenta = cuentaConSaldo("100.00");

        assertThrows(IllegalArgumentException.class, () -> cuenta.acreditar(new BigDecimal("-1")));
        assertEquals(0, new BigDecimal("100.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    @DisplayName("debitar resta el importe cuando el saldo alcanza")
    void debitarRestaElImporte() {
        Account cuenta = cuentaConSaldo("100.00");

        boolean resultado = cuenta.debitar(new BigDecimal("40.00"));

        assertTrue(resultado);
        assertEquals(0, new BigDecimal("60.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    @DisplayName("debitar permite dejar el saldo exactamente en cero")
    void debitarPermiteSaldoCero() {
        Account cuenta = cuentaConSaldo("100.00");

        boolean resultado = cuenta.debitar(new BigDecimal("100.00"));

        assertTrue(resultado);
        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getSaldo()));
    }

    @Test
    @DisplayName("debitar devuelve false y no cambia el saldo si no alcanza")
    void debitarConSaldoInsuficienteDevuelveFalse() {
        Account cuenta = cuentaConSaldo("100.00");

        boolean resultado = cuenta.debitar(new BigDecimal("100.01"));

        assertFalse(resultado);
        assertEquals(0, new BigDecimal("100.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    @DisplayName("debitar rechaza un importe nulo")
    void debitarRechazaImporteNulo() {
        Account cuenta = cuentaConSaldo("100.00");

        assertThrows(IllegalArgumentException.class, () -> cuenta.debitar(null));
    }

    @Test
    @DisplayName("debitar rechaza un importe en cero")
    void debitarRechazaImporteCero() {
        Account cuenta = cuentaConSaldo("100.00");

        assertThrows(IllegalArgumentException.class, () -> cuenta.debitar(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("debitar rechaza un importe negativo")
    void debitarRechazaImporteNegativo() {
        Account cuenta = cuentaConSaldo("100.00");

        assertThrows(IllegalArgumentException.class, () -> cuenta.debitar(new BigDecimal("-5")));
        assertEquals(0, new BigDecimal("100.00").compareTo(cuenta.getSaldo()));
    }
}
