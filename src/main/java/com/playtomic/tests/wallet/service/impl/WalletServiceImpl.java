package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.client.StripeService;
import com.playtomic.tests.wallet.dto.PaymentDto;
import com.playtomic.tests.wallet.entity.Transaction;
import com.playtomic.tests.wallet.entity.TransactionStatus;
import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.exception.PaymentFailedException;
import com.playtomic.tests.wallet.exception.StripeServiceException;
import com.playtomic.tests.wallet.exception.WalletNotFoundException;
import com.playtomic.tests.wallet.repository.TransactionRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.PaymentService;
import com.playtomic.tests.wallet.service.WalletService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final PaymentService paymentService;

  public WalletServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository,
      StripeService stripeService, PaymentService paymentService) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.paymentService = paymentService;
  }

  @Override
  public Wallet getWallet(Long id) {
    return walletRepository.findById(id).orElseThrow(WalletNotFoundException::new);
  }

  @Override
  @Transactional
  public Wallet topUpWallet(Long id, String creditCard, BigDecimal amount, String idempotencyKey) {
    // Validate existence of wallet
    Wallet wallet = getWallet(id);

    // Validate if idempotentKey is present for that wallet
    if (idempotencyKey.equals(wallet.getIdempotencyKey())) {
      return wallet;
    }

    // Create new transaction with pending status
    Transaction transaction = new Transaction();
    transaction.setWallet(wallet);
    transaction.setAmount(amount);
    transaction.setTransactionDate(LocalDateTime.now());
    transaction.setStatus(TransactionStatus.PENDING);

    Transaction newTransaction = this.transactionRepository.save(transaction);

    // Call payment service
    try {
      PaymentDto payment = paymentService.charge(creditCard, amount);
      newTransaction.setPaymentId(payment.id());
      newTransaction.setStatus(TransactionStatus.COMPLETED);
    } catch (StripeServiceException e) {
      log.error("Payment process failed during top up wallet {} , details: {}", id, e.getMessage());
      newTransaction.setStatus(TransactionStatus.FAILED);
      throw new PaymentFailedException(e.getMessage());
    }

    // Update transaction status and add payment id
    this.transactionRepository.save(newTransaction);

    // Update balance wallet
    wallet.setBalance(wallet.getBalance().add(amount));
    wallet.setIdempotencyKey(idempotencyKey);

    return walletRepository.save(wallet);
  }
}
