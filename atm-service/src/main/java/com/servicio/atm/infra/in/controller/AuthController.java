package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.LoginService;
import com.servicio.atm.domain.model.in.LoginRequest;
import com.servicio.atm.domain.model.out.ErrorResponse;
import com.servicio.atm.domain.model.out.LoginResponse;
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

/**
 * Expone la operacion de identificacion con tarjeta.
 */
@RestController
@RequestMapping("/api/login")
@Tag(name = "Autenticacion", description = "Identificacion del usuario con su tarjeta")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Verifica que la tarjeta exista y este activa.
     *
     * @param request numero de tarjeta
     * @return {@code success=true} si el ingreso es exitoso, {@code false} en caso contrario
     */
    @Operation(summary = "Identificarse con el numero de tarjeta",
            description = "Responde success=true si la tarjeta existe y esta activa; "
                    + "en cualquier otro caso responde success=false.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado de la identificacion"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        boolean success = loginService.login(request.getCardNumber());
        return ResponseEntity.ok(new LoginResponse(success));
    }
}