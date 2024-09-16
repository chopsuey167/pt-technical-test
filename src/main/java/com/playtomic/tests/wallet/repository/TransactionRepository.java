package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}