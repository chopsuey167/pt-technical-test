package com.playtomic.tests.wallet.controller;

import com.playtomic.tests.wallet.dto.WalletResponseDto;
import com.playtomic.tests.wallet.dto.WalletTopUpRequestDto;
import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.mapper.WalletDtoMapper;
import com.playtomic.tests.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
  public ResponseEntity<WalletResponseDto> getWallet(@PathVariable Long id) {
    Wallet wallet = walletService.getWallet(id);
    return ResponseEntity.ok(walletDtoMapper.toWalletResponseDto(wallet));
  }

  @PostMapping("/{id}/top-up")
  public ResponseEntity<WalletResponseDto> topUpWallet(@PathVariable Long id,
      @Valid @RequestBody WalletTopUpRequestDto topUpRequestDto,
      @RequestHeader("Idempotency-Key") String idempotencyKey) {
    Wallet wallet = walletService.topUpWallet(id, topUpRequestDto.creditCard(), topUpRequestDto.amount(),
        idempotencyKey);
    return ResponseEntity.ok(walletDtoMapper.toWalletResponseDto(wallet));
  }
}
