package com.playtomic.tests.wallet.service.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.playtomic.tests.wallet.client.StripeRestTemplateResponseErrorHandler;
import com.playtomic.tests.wallet.client.StripeService;
import com.playtomic.tests.wallet.dto.PaymentDto;
import com.playtomic.tests.wallet.exception.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.exception.StripeServiceException;
import java.math.BigDecimal;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * This test is failing with the current implementation.
 * <p>
 * How would you test this?
 */
@ExtendWith(MockitoExtension.class)
public class StripeServiceTest {

  public static final String CREDIT_CARD_NUMBER = "4242 4242 4242 4242";
  private static final URI testUri = URI.create("http://how-would-you-test-me.localhost");
  @Mock
  private RestTemplate restTemplate;
  @Mock
  private RestTemplateBuilder restTemplateBuilder;
  private StripeService stripeService;

  @BeforeEach
  void setUp() {
    when(restTemplateBuilder.errorHandler(any(StripeRestTemplateResponseErrorHandler.class))).thenReturn(
        restTemplateBuilder);
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
    stripeService = new StripeService(testUri, testUri, restTemplateBuilder);
  }

  @Test
  public void test_exception() {
    //given
    when(restTemplate.postForObject(eq(testUri), any(), eq(PaymentDto.class))).thenThrow(
        StripeAmountTooSmallException.class);
    //when
    Executable executable = () -> stripeService.charge(CREDIT_CARD_NUMBER, new BigDecimal(5));
    //then
    var exception = assertThrows(StripeAmountTooSmallException.class, executable);
    assertEquals(StripeAmountTooSmallException.class, exception.getClass());
  }

  @Test
  public void test_ok() throws StripeServiceException {
    //given
    PaymentDto paymentDto = new PaymentDto("1");

    when(restTemplate.postForObject(eq(testUri), any(), eq(PaymentDto.class))).thenReturn(paymentDto);
    //when
    PaymentDto actual = stripeService.charge(CREDIT_CARD_NUMBER, new BigDecimal(15));

    //then
    assertNotNull(actual);
    assertEquals(paymentDto, actual);
  }
}
