package com.servicio.atm.domain.model.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Respuesta de la consulta de saldo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
     private BigDecimal balance;
}