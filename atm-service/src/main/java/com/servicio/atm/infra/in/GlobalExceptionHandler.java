package com.servicio.atm.infra.in;

import com.servicio.atm.domain.exception.CuentaNoEncontradaException;
import com.servicio.atm.domain.exception.SaldoInsuficienteException;
import com.servicio.atm.domain.exception.TarjetaInactivaException;
import com.servicio.atm.domain.exception.TarjetaNoEncontradaException;
import com.servicio.atm.domain.model.out.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones del servicio a respuestas HTTP con un formato comun.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400: el body no cumple las validaciones (@NotBlank, @NotNull, @DecimalMin...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return build(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", details);
    }

    /** 400: JSON mal formado o tipo incorrecto (por ejemplo "amount": "abc"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es valido", Collections.emptyList());
    }

    /** 400: validaciones del dominio, como el importe en Account.validarImporte. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), Collections.emptyList());
    }

    /** 404: la cuenta no existe o no corresponde a la tarjeta indicada. */
    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaNoEncontrada(CuentaNoEncontradaException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), Collections.emptyList());
    }

    /** 403: la tarjeta existe pero esta inactiva. */
    @ExceptionHandler(TarjetaInactivaException.class)
    public ResponseEntity<ErrorResponse> handleTarjetaInactiva(TarjetaInactivaException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), Collections.emptyList());
    }

    /** 404: el numero de tarjeta no existe. */
    @ExceptionHandler(TarjetaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleTarjetaNoEncontrada(TarjetaNoEncontradaException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), Collections.emptyList());
    }

    /** 405: el metodo HTTP no esta permitido en ese endpoint (por ejemplo GET en lugar de POST). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP no permitido para este recurso", Collections.emptyList());
    }

    /** 415: el Content-Type de la solicitud no es soportado (se espera application/json). */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type no soportado, se espera application/json", Collections.emptyList());
    }

    /** 422: la operacion es valida pero el saldo no alcanza. */
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), Collections.emptyList());
    }

    /** 503: no se pudo obtener el bloqueo de la cuenta a tiempo; es un fallo temporal, se puede reintentar. */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleLockFailure(PessimisticLockingFailureException ex) {
        log.warn("No se pudo obtener el bloqueo de la cuenta", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "El servicio esta ocupado, intente nuevamente", Collections.emptyList());
    }

    /** 500: cualquier error no previsto. Se registra completo en el log, pero al cliente no se le envia la traza. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servicio", Collections.emptyList());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message, details));
    }
}