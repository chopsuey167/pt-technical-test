package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.client.StripeService;
import com.playtomic.tests.wallet.entity.Transaction;
import com.playtomic.tests.wallet.entity.TransactionStatus;
import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.exception.WalletNotFoundException;
import com.playtomic.tests.wallet.repository.TransactionRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.WalletService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final StripeService stripeService;

  public WalletServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository,
      StripeService stripeService) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.stripeService = stripeService;
  }

  @Override
  public Wallet getWallet(Long id) {
    return walletRepository.findById(id).orElseThrow(WalletNotFoundException::new);
  }

  @Override
  @Transactional
  public Wallet topUpWallet(Long id, String creditCard, BigDecimal amount) {
    // validate existence of wallet
    Wallet wallet = getWallet(id);

    // create new transaction status pending
    Transaction transaction = new Transaction();
    transaction.setWallet(wallet);
    transaction.setAmount(amount);
    transaction.setTransactionDate(LocalDateTime.now());
    transaction.setStatus(TransactionStatus.PENDING);

    Transaction newTransaction = this.transactionRepository.save(transaction);
    // call payment service
    stripeService.charge(creditCard, amount);
    // update transaction status
    newTransaction.setStatus(TransactionStatus.COMPLETED);
    // update balance wallet
    wallet.setBalance(wallet.getBalance().add(amount));

    return walletRepository.save(wallet);
  }
}
