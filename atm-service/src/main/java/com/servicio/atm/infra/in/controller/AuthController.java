package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.LoginService;
import com.servicio.atm.domain.model.in.LoginRequest;
import com.servicio.atm.domain.model.out.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/login")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        boolean success = loginService.login(request.getCardNumber());
        return ResponseEntity.ok(new LoginResponse(success));
    }
}