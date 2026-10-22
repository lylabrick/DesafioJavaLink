package com.servicio.atm.infra.out.persistence;

import com.servicio.atm.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

/**
 * Acceso a cuentas. Las busquedas "ForUpdate" toman un bloqueo pesimista de
 * escritura (SELECT ... FOR UPDATE) y deben usarse dentro de una transaccion.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Busca una cuenta por id bloqueando la fila hasta el fin de la transaccion.
     *
     * @param id identificador de la cuenta
     * @return la cuenta, si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    /**
     * Busca una cuenta por CBU bloqueando la fila hasta el fin de la transaccion.
     *
     * @param cbu CBU de la cuenta
     * @return la cuenta, si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.cbu = :cbu")
    Optional<Account> findByCbuForUpdate(@Param("cbu") String cbu);
}