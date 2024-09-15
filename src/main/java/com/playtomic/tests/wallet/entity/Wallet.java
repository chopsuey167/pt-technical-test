package com.playtomic.tests.wallet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.Data;

@Entity
@Data
public class Wallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private BigDecimal balance;
}
