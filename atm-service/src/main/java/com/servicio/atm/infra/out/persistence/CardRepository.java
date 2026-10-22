package com.servicio.atm.infra.out.persistence;

import com.servicio.atm.domain.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a tarjetas.
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Busca una tarjeta por su numero.
     *
     * @param numero numero de tarjeta
     * @return la tarjeta, si existe
     */
    Optional<Card> findByNumero(String numero);
}
