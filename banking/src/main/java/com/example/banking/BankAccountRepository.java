package com.example.banking;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
}

