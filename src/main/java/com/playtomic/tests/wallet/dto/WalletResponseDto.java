package com.playtomic.tests.wallet.dto;

import java.math.BigDecimal;

public record WalletResponseDto(Long id, BigDecimal balance) {
}
