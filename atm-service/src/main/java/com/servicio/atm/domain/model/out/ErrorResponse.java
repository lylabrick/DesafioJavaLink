package com.servicio.atm.domain.model.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato unico de error que reciben los clientes del servicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;

    public static ErrorResponse of(int status, String error, String message, List<String> details) {
        return new ErrorResponse(status, error, message, details, LocalDateTime.now());
    }
}