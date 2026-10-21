package com.servicio.atm.infra.out.persistence;

import com.servicio.atm.domain.entity.CardAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardAccountRepository extends JpaRepository<CardAccount, Long> {

    Optional<CardAccount> findByTarjeta_NumeroAndCuenta_Numero(
            String cardNumber,
            String accountNumber);
}