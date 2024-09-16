package com.playtomic.tests.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

  public static final String IDEMPOTENCY_KEY = "123";
  public static final BigDecimal AMOUNT = BigDecimal.valueOf(15);
  private static final Long WALLET_ID = 1L;
  private static final String CREDIT_CARD_NUMBER = "376227101160682";
  @InjectMocks
  private WalletServiceImpl walletService;
  @Mock
  private WalletRepository walletRepository;
  @Mock
  private TransactionRepository transactionRepository;
  @Mock
  private PaymentService paymentService;

  private static Wallet buildWallet() {
    Wallet wallet = new Wallet();
    wallet.setId(1L);
    wallet.setBalance(BigDecimal.valueOf(1000));
    return wallet;
  }

  private static Transaction buildNewTransaction(Wallet wallet) {
    Transaction transaction = new Transaction();
    transaction.setId(1L);
    transaction.setWallet(wallet);
    transaction.setAmount(AMOUNT);
    transaction.setTransactionDate(LocalDateTime.now());
    transaction.setStatus(TransactionStatus.PENDING);
    return transaction;
  }

  @Test
  void getWallet_existingId_returnWallet() {
    //given
    Wallet wallet = buildWallet();

    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));

    //when
    Wallet actual = walletService.getWallet(WALLET_ID);

    //then
    assertNotNull(actual);
    assertEquals(wallet, actual);

    verify(walletRepository, times(1)).findById(any());
  }

  @Test
  void getWallet_noExistingId_throwNotFoundException() {
    //given
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.empty());

    //when
    Executable executable = () -> walletService.getWallet(WALLET_ID);

    //then
    var exception = assertThrows(WalletNotFoundException.class, executable);
    assertEquals(WalletNotFoundException.class, exception.getClass());

    verify(walletRepository, times(1)).findById(any());
  }

  @Test
  void topUpWallet_notExistingWalletId_throwNotFoundException() {
    //given
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.empty());

    //when
    Executable executable = () -> walletService.topUpWallet(WALLET_ID, CREDIT_CARD_NUMBER, AMOUNT,
        IDEMPOTENCY_KEY);

    //then
    var exception = assertThrows(WalletNotFoundException.class, executable);
    assertEquals(WalletNotFoundException.class, exception.getClass());

    verify(walletRepository, times(1)).findById(any());
    verify(walletRepository, never()).save(any());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void topUpWallet_existingIdempotentKey_returnWallet() {
    //given
    Wallet wallet = buildWallet();
    wallet.setIdempotencyKey(IDEMPOTENCY_KEY);

    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));

    //when
    Wallet actual = walletService.topUpWallet(WALLET_ID, CREDIT_CARD_NUMBER, AMOUNT,
        IDEMPOTENCY_KEY);

    //then
    assertNotNull(actual);
    assertEquals(wallet, actual);

    verify(walletRepository, times(1)).findById(any());
    verify(walletRepository, never()).save(any());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void topUpWallet_paymentServiceFailed_throwPaymentFailedException() {
    //given
    Wallet wallet = buildWallet();

    Transaction transaction = buildNewTransaction(wallet);

    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
    when(transactionRepository.save(any())).thenReturn(transaction);
    when(paymentService.charge(CREDIT_CARD_NUMBER, AMOUNT)).thenThrow(StripeServiceException.class);

    //when
    Executable executable = () -> walletService.topUpWallet(WALLET_ID, CREDIT_CARD_NUMBER, AMOUNT,
        IDEMPOTENCY_KEY);

    //then
    var exception = assertThrows(PaymentFailedException.class, executable);
    assertEquals(PaymentFailedException.class, exception.getClass());

    verify(walletRepository, times(1)).findById(any());
    verify(walletRepository, never()).save(any());
    verify(transactionRepository, times(1)).save(any());

  }

  @Test
  void topUpWallet_happyPath_returnWallet() {
    //given
    Wallet wallet = buildWallet();
    Wallet walletFinal = buildWallet();
    walletFinal.setIdempotencyKey(IDEMPOTENCY_KEY);
    walletFinal.setBalance(wallet.getBalance().add(AMOUNT));

    Transaction transaction = buildNewTransaction(wallet);
    Transaction transactionFinal = buildNewTransaction(wallet);
    transactionFinal.setPaymentId("1");
    transactionFinal.setStatus(TransactionStatus.COMPLETED);

    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
    when(transactionRepository.save(any())).thenReturn(transaction).thenReturn(transactionFinal);
    when(paymentService.charge(CREDIT_CARD_NUMBER, AMOUNT)).thenReturn(new PaymentDto("1"));
    when(walletRepository.save(wallet)).thenReturn(wallet).thenReturn(walletFinal);

    //when
    Wallet actual = walletService.topUpWallet(WALLET_ID, CREDIT_CARD_NUMBER, AMOUNT,
        IDEMPOTENCY_KEY);

    //then
    assertNotNull(actual);
    assertEquals(wallet, actual);
    assertEquals(walletFinal.getBalance(), actual.getBalance());

    verify(walletRepository, times(1)).findById(any());
    verify(walletRepository, times(1)).save(any());
    verify(transactionRepository, times(2)).save(any());
  }
}