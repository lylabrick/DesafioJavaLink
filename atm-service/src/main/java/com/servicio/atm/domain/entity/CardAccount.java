package com.servicio.atm.domain.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Representa la asociacion entre una tarjeta y una cuenta.
 *
 * <p>Una misma pareja de tarjeta y cuenta no puede repetirse.</p>
 */
@Entity
@Table(
        name = "tarjeta_cuenta",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tarjeta_cuenta",
                        columnNames = {"tarjeta_id", "cuenta_id"}
                )
        }
)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CardAccount {

    /** Identificador interno de la asociacion. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tarjeta asociada a la cuenta. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarjeta_id", nullable = false)
    private Card tarjeta;

    /** Cuenta asociada a la tarjeta. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Account cuenta;

}
