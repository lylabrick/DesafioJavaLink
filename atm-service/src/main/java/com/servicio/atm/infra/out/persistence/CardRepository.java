package com.servicio.atm.infra.out.persistence;

import com.servicio.atm.domain.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByNumero(String numero);
}
