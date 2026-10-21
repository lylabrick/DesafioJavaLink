package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.BalanceService;
import com.servicio.atm.domain.model.in.BalanceRequest;
import com.servicio.atm.domain.model.out.BalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final BalanceService balanceService;

    public AccountController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @PostMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @Valid @RequestBody BalanceRequest request) {

        BigDecimal balance = balanceService.getBalance(
                request.getCardNumber(),
                request.getAccountNumber());

        return ResponseEntity.ok(new BalanceResponse(balance));
    }

}