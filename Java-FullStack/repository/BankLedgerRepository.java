package com.jetlease.repository;

import com.jetlease.entity.BankLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankLedgerRepository extends JpaRepository<BankLedger, Long> {
    Optional<BankLedger> findByTransactionIdAndBookingId(String transactionId, String bookingId);
}
