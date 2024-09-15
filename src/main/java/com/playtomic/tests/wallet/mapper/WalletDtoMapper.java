package com.playtomic.tests.wallet.mapper;

import com.playtomic.tests.wallet.dto.WalletResponseDto;
import com.playtomic.tests.wallet.entity.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletDtoMapper {

  WalletResponseDto toWalletResponseDto(Wallet wallet);
}
