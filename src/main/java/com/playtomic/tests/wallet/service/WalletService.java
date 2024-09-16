package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.entity.Wallet;
import java.math.BigDecimal;

public interface WalletService {

  Wallet getWallet(Long id);

  Wallet topUpWallet(Long id, String creditCard, BigDecimal amount, String idempotencyKey);
}
