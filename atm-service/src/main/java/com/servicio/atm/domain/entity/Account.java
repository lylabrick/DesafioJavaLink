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

/**
 * Representa una cuenta bancaria y su saldo.
 *
 * <p>El identificador interno se genera en la base de datos. El numero y el CBU
 * identifican la cuenta desde el punto de vista del negocio.</p>
 */
@Entity
@Table(name = "cuentas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account {

    /** Identificador interno de la cuenta. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero de cuenta, unico y obligatorio. */
    @Column(nullable = false, unique = true, length = 22)
    private String numero;

    /** CBU unico y obligatorio de la cuenta. */
    @Column(nullable = false, unique = true, length = 22)
    private String cbu;

    /** Saldo actual de la cuenta, expresado con dos decimales. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    /**
     * Aumenta el saldo de la cuenta por el importe indicado.
     *
     * @param importe importe que se acredita
     */
    public void acreditar(BigDecimal importe) {
        this.saldo = this.saldo.add(importe);
    }

    /**
     * Intenta reducir el saldo por el importe indicado.
     *
     * @param importe importe que se debita
     * @return {@code true} si el saldo alcanzaba y se realizo el debito;
     *         {@code false} si el saldo era insuficiente
     */
    public boolean debitar(BigDecimal importe) {
        if (this.saldo.compareTo(importe) < 0) {
            return false;
        }

        this.saldo = this.saldo.subtract(importe);
        return true;
    }
}
