package com.playtomic.tests.wallet.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.dto.WalletResponseDto;
import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {

  private static final Long WALLET_ID = 1L;
  private static ObjectMapper mapper = new ObjectMapper();
  @Autowired
  private WalletRepository walletRepository;
  @Autowired
  private MockMvc mvc;

  @BeforeEach
  public void beforeEach() {
    this.walletRepository.deleteAll();
  }

  @Test
  void getWallet_existingId_success() throws Exception {
    //given
    Wallet wallet = new Wallet();
    wallet.setId(1L);
    wallet.setBalance(BigDecimal.valueOf(1000));
    this.walletRepository.saveAndFlush(wallet);

    WalletResponseDto expectedResponse = new WalletResponseDto(1L, BigDecimal.valueOf(1000));

    //when
    ResultActions response = mvc.perform(get("/wallets/" + WALLET_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    //then
    response.andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
  }

  @Test
  void getWallet_noExistingId_throwNotFoundException() throws Exception {
    //when
    ResultActions response = mvc.perform(get("/wallets/" + WALLET_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    //then
    response.andDo(print())
        .andExpect(status().isNotFound());
  }
}