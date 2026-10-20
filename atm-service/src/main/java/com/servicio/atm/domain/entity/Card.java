package com.servicio.atm.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Representa una tarjeta bancaria.
 *
 * <p>El identificador interno se genera en la base de datos y el numero de
 * tarjeta es unico.</p>
 */
@Entity
@Table(name = "tarjetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    /** Identificador interno de la tarjeta. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero unico de la tarjeta. */
    @Column(name = "numero", nullable = false, unique = true, length = 19)
    private String numero;

    /** Indica si la tarjeta esta activa. */
    @Column(name = "activa", nullable = false)
    private Boolean activa;

}
