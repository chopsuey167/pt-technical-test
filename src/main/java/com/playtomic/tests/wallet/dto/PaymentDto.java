package com.playtomic.tests.wallet.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;

public record PaymentDto(@NonNull String id) {

  @JsonCreator
  public PaymentDto(@JsonProperty(value = "id", required = true) String id) {
    this.id = id;
  }
}
