package com.playtomic.tests.wallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WalletTopUpRequestDto(@NotNull @JsonProperty(value = "credit_card") String creditCard,
                                    @NotNull @JsonProperty(value = "amount") BigDecimal amount) {
}
