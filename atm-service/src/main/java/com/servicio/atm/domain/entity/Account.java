package com.servicio.atm.domain.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cuentas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 22)
    private String numero;

    @Column(nullable = false, unique = true, length = 22)
    private String cbu;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    public void acreditar(BigDecimal importe) {
        validarImporte(importe);
        saldo = saldo.add(importe);
    }

    public boolean debitar(BigDecimal importe) {
        validarImporte(importe);

        if (saldo.compareTo(importe) < 0) {
            return false;
        }

        saldo = saldo.subtract(importe);
        return true;
    }

    private void validarImporte(BigDecimal importe) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero");
        }
    }
}