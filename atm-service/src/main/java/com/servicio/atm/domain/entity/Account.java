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
    private Long numero;

    @Column(nullable = false, unique = true, length = 22)
    private String cbu;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;


    public void acreditar(BigDecimal importe) {
        this.saldo = this.saldo.add(importe);
    }

    public boolean debitar(BigDecimal importe) {
        if (this.saldo.compareTo(importe) < 0) {
            return false;
        }

        this.saldo = this.saldo.subtract(importe);
        return true;
    }
}