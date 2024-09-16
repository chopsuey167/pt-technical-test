package com.playtomic.tests.wallet.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Version;

@Entity
@Data
public class Wallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private BigDecimal balance;
  private String idempotencyKey;
  @Version
  private Long version;

  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
  private List<Transaction> transactions = new ArrayList<>();
}
