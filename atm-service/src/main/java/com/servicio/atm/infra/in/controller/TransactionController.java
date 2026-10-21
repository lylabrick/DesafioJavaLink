package com.servicio.atm.infra.in.controller;

import com.servicio.atm.app.service.DepositService;
import com.servicio.atm.app.service.ExtractService;
import com.servicio.atm.domain.model.in.DepositRequest;
import com.servicio.atm.domain.model.in.ExtractRequest;
import com.servicio.atm.domain.model.out.DepositResponse;
import com.servicio.atm.domain.model.out.ExtractResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final ExtractService extractService;
    private final DepositService depositService;

    public TransactionController(ExtractService extractService, DepositService depositService) {
        this.extractService = extractService;
        this.depositService = depositService;
    }

    @PostMapping("/extract")
    public ResponseEntity<ExtractResponse> extract(@Valid @RequestBody ExtractRequest request) {
        BigDecimal balance = extractService.extract(
                request.getCardNumber(),
                request.getAccountNumber(),
                request.getAmount());

        return ResponseEntity.ok(new ExtractResponse(request.getAmount(), balance));
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {
        BigDecimal balance = depositService.deposit(
                request.getCardNumber(),
                request.getCbu(),
                request.getAmount());

        return ResponseEntity.ok(new DepositResponse(request.getAmount(), balance));
    }
}