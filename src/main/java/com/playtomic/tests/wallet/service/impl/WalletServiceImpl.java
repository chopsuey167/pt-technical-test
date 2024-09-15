package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.exception.WalletNotFoundException;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.WalletService;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;

  public WalletServiceImpl(WalletRepository walletRepository) {this.walletRepository = walletRepository;}

  @Override
  public Wallet getWallet(Long id) {
    return walletRepository.findById(id).orElseThrow(WalletNotFoundException::new);
  }
}
