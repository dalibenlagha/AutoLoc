package tn.esprit.autoloc.web.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.autoloc.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return erreur(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return erreur(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> champs = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            champs.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ResponseEntity<Map<String, Object>> reponse =
                erreur(HttpStatus.BAD_REQUEST, "Données invalides");
        reponse.getBody().put("erreurs", champs);
        return reponse;
    }

    private ResponseEntity<Map<String, Object>> erreur(HttpStatus statut, String message) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("statut", statut.value());
        corps.put("message", message);
        corps.put("horodatage", LocalDateTime.now(ZoneId.systemDefault()).toString());
        return ResponseEntity.status(statut).body(corps);
    }
}
