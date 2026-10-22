package com.servicio.atm.domain.model.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Respuesta del deposito: importe acreditado y saldo resultante de la cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {
    private BigDecimal amount;
    private BigDecimal balance;
}