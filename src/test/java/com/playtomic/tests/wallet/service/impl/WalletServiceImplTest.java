package com.playtomic.tests.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.exception.WalletNotFoundException;
import com.playtomic.tests.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

  private static final Long WALLET_ID = 1L;
  @InjectMocks
  private WalletServiceImpl walletService;
  @Mock
  private WalletRepository walletRepository;

  @Test
  void getWallet_existingId_returnWallet() {
    //given
    Wallet wallet = new Wallet();
    wallet.setId(1L);
    wallet.setBalance(BigDecimal.valueOf(1000));

    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));

    //when
    Wallet actual = walletService.getWallet(WALLET_ID);

    //then
    assertNotNull(actual);
    assertEquals(wallet, actual);
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
  }
}