package com.playtomic.tests.wallet.dto;

import java.math.BigDecimal;
import lombok.NonNull;

public record WalletTopUpRequestDto(@NonNull String creditCard,
                                    @NonNull BigDecimal amount) {
}
