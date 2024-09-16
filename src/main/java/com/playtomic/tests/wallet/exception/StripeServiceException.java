package com.playtomic.tests.wallet.exception;

public class StripeServiceException extends RuntimeException {

  public StripeServiceException(String message) {
    super(message);
  }
}
