package com.playtomic.tests.wallet.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;

public class PaymentDto {

    @NonNull
    private String id;

    @JsonCreator
    public PaymentDto(@JsonProperty(value = "id", required = true) String id) {
        this.id = id;
    }
}
