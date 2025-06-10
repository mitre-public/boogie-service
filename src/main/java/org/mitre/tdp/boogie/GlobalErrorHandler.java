package org.mitre.tdp.boogie;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class GlobalErrorHandler {

  @ExceptionHandler(Exception.class)
  ResponseEntity<Object> handleException(Exception e) {
    e.printStackTrace();
    return ResponseEntity.internalServerError().body(e.getMessage());
  }
}
