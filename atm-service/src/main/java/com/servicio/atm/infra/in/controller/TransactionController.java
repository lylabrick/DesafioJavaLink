package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.DepositService;
import com.servicio.atm.app.service.ExtractService;
import com.servicio.atm.domain.model.in.DepositRequest;
import com.servicio.atm.domain.model.in.ExtractRequest;
import com.servicio.atm.domain.model.out.DepositResponse;
import com.servicio.atm.domain.model.out.ErrorResponse;
import com.servicio.atm.domain.model.out.ExtractResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * Expone las operaciones que modifican el saldo: extraccion y deposito.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transacciones", description = "Extraccion y deposito de efectivo")
public class TransactionController {

    private final ExtractService extractService;
    private final DepositService depositService;

    public TransactionController(ExtractService extractService, DepositService depositService) {
        this.extractService = extractService;
        this.depositService = depositService;
    }

    /**
     * Extrae efectivo de una cuenta asociada a la tarjeta indicada.
     *
     * @param request numero de tarjeta, numero de cuenta e importe a retirar
     * @return importe retirado y saldo resultante
     */
    @Operation(summary = "Extraer efectivo",
            description = "Debita el importe de la cuenta si el saldo alcanza (el saldo no puede quedar negativo).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Extraccion realizada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "La tarjeta esta inactiva",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La cuenta no existe o no corresponde a la tarjeta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio ocupado, se puede reintentar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/extract")
    public ResponseEntity<ExtractResponse> extract(@Valid @RequestBody ExtractRequest request) {
        BigDecimal balance = extractService.extract(
                request.getCardNumber(),
                request.getAccountNumber(),
                request.getAmount());

        return ResponseEntity.ok(new ExtractResponse(request.getAmount(), balance));
    }

    /**
     * Deposita efectivo en la cuenta identificada por su CBU. La cuenta puede
     * pertenecer o no al titular de la tarjeta.
     *
     * @param request numero de tarjeta, CBU destino e importe a depositar
     * @return importe depositado y saldo resultante
     */
    @Operation(summary = "Depositar efectivo",
            description = "Acredita el importe en la cuenta que corresponde al CBU indicado. "
                    + "La cuenta puede no pertenecer al titular de la tarjeta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposito realizado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "La tarjeta esta inactiva",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta o la cuenta con ese CBU no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio ocupado, se puede reintentar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {
        BigDecimal balance = depositService.deposit(
                request.getCardNumber(),
                request.getCbu(),
                request.getAmount());

        return ResponseEntity.ok(new DepositResponse(request.getAmount(), balance));
    }
}