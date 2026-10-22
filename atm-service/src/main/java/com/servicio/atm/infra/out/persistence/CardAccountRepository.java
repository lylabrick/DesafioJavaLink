package com.servicio.atm.infra.out.persistence;

import com.servicio.atm.domain.entity.CardAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a la asociacion entre tarjetas y cuentas.
 */
public interface CardAccountRepository extends JpaRepository<CardAccount, Long> {

    /**
     * Busca la asociacion entre una tarjeta y una cuenta por sus numeros.
     *
     * @param cardNumber    numero de tarjeta
     * @param accountNumber numero de cuenta
     * @return la asociacion, si existe
     */
    Optional<CardAccount> findByTarjeta_NumeroAndCuenta_Numero(
            String cardNumber,
            String accountNumber);
}