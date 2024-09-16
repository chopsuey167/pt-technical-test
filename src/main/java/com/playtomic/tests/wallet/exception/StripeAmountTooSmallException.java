package com.playtomic.tests.wallet.exception;

public class StripeAmountTooSmallException extends StripeServiceException {

  public StripeAmountTooSmallException() {
    super("Amount requested is less than $10");
  }
}
