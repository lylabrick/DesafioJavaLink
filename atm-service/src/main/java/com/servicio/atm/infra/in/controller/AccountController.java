package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.BalanceService;
import com.servicio.atm.domain.model.in.BalanceRequest;
import com.servicio.atm.domain.model.out.BalanceResponse;
import com.servicio.atm.domain.model.out.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * Expone las consultas sobre cuentas.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Cuentas", description = "Consulta de saldo")
public class AccountController {

    private final BalanceService balanceService;

    public AccountController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    /**
     * Consulta el saldo de una cuenta asociada a la tarjeta indicada.
     *
     * @param request numero de tarjeta y numero de cuenta
     * @return saldo actual de la cuenta
     */
    @Operation(summary = "Consultar saldo",
            description = "Devuelve el saldo de la cuenta si esta asociada a la tarjeta indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo de la cuenta"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La cuenta no existe o no corresponde a la tarjeta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @Valid @RequestBody BalanceRequest request) {

        BigDecimal balance = balanceService.getBalance(
                request.getCardNumber(),
                request.getAccountNumber());

        return ResponseEntity.ok(new BalanceResponse(balance));
    }

}