package com.playtomic.tests.wallet.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.dto.PaymentDto;
import com.playtomic.tests.wallet.dto.WalletResponseDto;
import com.playtomic.tests.wallet.dto.WalletTopUpRequestDto;
import com.playtomic.tests.wallet.entity.Transaction;
import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.exception.StripeServiceException;
import com.playtomic.tests.wallet.repository.TransactionRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.PaymentService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
@DirtiesContext
class WalletControllerTest {

  public static final String CREDIT_CARD_NUMBER = "343255295919167";
  private static final Long NO_EXISTING_WALLET_ID = 2L;
  private static ObjectMapper mapper = new ObjectMapper();
  @Autowired
  private WalletRepository walletRepository;
  @Autowired
  private TransactionRepository transactionRepository;
  @Autowired
  private MockMvc mvc;
  @MockBean
  private PaymentService paymentService;

  @BeforeEach
  public void beforeEach() {
    walletRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  @Test
  void getWallet_existingId_success() throws Exception {
    //given
    Wallet wallet = createNewWallet();
    WalletResponseDto expectedResponse = new WalletResponseDto(wallet.getId(), BigDecimal.valueOf(1000));

    //when
    ResultActions response = mvc.perform(get("/wallets/" + wallet.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    //then
    response.andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
  }

  private Wallet createNewWallet() {
    Wallet wallet = new Wallet();
    wallet.setIdempotencyKey("1234");
    wallet.setBalance(BigDecimal.valueOf(1000.00));
    return walletRepository.save(wallet);
  }

  @Test
  void getWallet_noExistingId_throwNotFoundException() throws Exception {
    //when
    ResultActions response = mvc.perform(get("/wallets/" + NO_EXISTING_WALLET_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    //then
    response.andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void topUpWallet_existingId_success() throws Exception {
    //Given
    Wallet wallet = createNewWallet();

    WalletResponseDto expectedResponse = new WalletResponseDto(wallet.getId(), BigDecimal.valueOf(1100.00));
    WalletTopUpRequestDto topUpRequestDto = new WalletTopUpRequestDto(CREDIT_CARD_NUMBER, BigDecimal.valueOf(100.00));

    when(paymentService.charge(any(), any())).thenReturn(new PaymentDto("1"));

    //when
    ResultActions response = mvc.perform(post("/wallets/" + wallet.getId() + "/top-up")
        .header("Idempotency-Key", "123")
        .content(mapper.writeValueAsString(topUpRequestDto))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    //then
    response.andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));

    List<Transaction> transaction = transactionRepository.findByWallet(wallet);
    assertEquals(1, transaction.size());
  }

  @Test
  void topUpWallet_existingIdempotentKey_returnWalletWithoutChanges() throws Exception {
    //Given
    Wallet wallet = createNewWallet();

    WalletResponseDto expectedResponse = new WalletResponseDto(wallet.getId(), BigDecimal.valueOf(1000.00));
    WalletTopUpRequestDto topUpRequestDto = new WalletTopUpRequestDto(CREDIT_CARD_NUMBER, BigDecimal.valueOf(100.00));

    when(paymentService.charge(any(), any())).thenReturn(new PaymentDto("1"));

    //when
    ResultActions response = mvc.perform(post("/wallets/" + wallet.getId() + "/top-up")
        .header("Idempotency-Key", "1234")
        .content(mapper.writeValueAsString(topUpRequestDto))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    //then
    response.andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));

    List<Transaction> transaction = transactionRepository.findByWallet(wallet);
    assertEquals(0, transaction.size());
  }

  @Test
  void topUpWallet_paymentFailed_returnUnprocessedEntityStatus() throws Exception {
    //Given
    Wallet wallet = createNewWallet();

    WalletTopUpRequestDto topUpRequestDto = new WalletTopUpRequestDto(CREDIT_CARD_NUMBER, BigDecimal.valueOf(100.00));

    when(paymentService.charge(topUpRequestDto.creditCard(), topUpRequestDto.amount())).thenThrow(
        StripeServiceException.class);

    //when
    ResultActions response = mvc.perform(post("/wallets/" + wallet.getId() + "/top-up")
        .header("Idempotency-Key", "123")
        .content(mapper.writeValueAsString(topUpRequestDto))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));

    //then
    response.andDo(print())
        .andExpect(status().isUnprocessableEntity());

    List<Transaction> transaction = transactionRepository.findByWallet(wallet);
    assertEquals(0, transaction.size());
  }


}