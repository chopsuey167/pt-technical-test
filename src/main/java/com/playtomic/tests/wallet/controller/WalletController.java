package com.playtomic.tests.wallet.controller;

import com.playtomic.tests.wallet.dto.WalletResponseDto;
import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.mapper.WalletDtoMapper;
import com.playtomic.tests.wallet.service.WalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
public class WalletController {

  private final WalletService walletService;
  private final WalletDtoMapper walletDtoMapper;

  public WalletController(WalletService walletService, WalletDtoMapper walletDtoMapper) {
    this.walletService = walletService;
    this.walletDtoMapper = walletDtoMapper;
  }

  @GetMapping("/{id}")
  public WalletResponseDto getWalletBalance(@PathVariable Long id) {
    Wallet wallet = walletService.getWallet(id);
    return walletDtoMapper.toWalletResponseDto(wallet);
  }
}
