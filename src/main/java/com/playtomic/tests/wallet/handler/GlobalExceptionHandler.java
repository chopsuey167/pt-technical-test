package com.playtomic.tests.wallet.handler;

import com.playtomic.tests.wallet.dto.ErrorDto;
import com.playtomic.tests.wallet.exception.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static ErrorDto buildErrorDto(String message, HttpStatus status) {
    return new ErrorDto(status, message);
  }

  @ExceptionHandler(WalletNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<?> handleMessageDecodingException(WalletNotFoundException e) {
    return new ResponseEntity<>(buildErrorDto(e.getMessage(), HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
  }

}
