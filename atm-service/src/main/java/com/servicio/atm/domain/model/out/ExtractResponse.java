package com.servicio.atm.domain.model.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Respuesta de la extraccion: importe retirado y saldo resultante de la cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractResponse {
    private BigDecimal amount;
    private BigDecimal balance;
}
