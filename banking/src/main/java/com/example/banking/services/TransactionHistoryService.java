package com.example.banking.services;

import com.example.banking.entity.BankAccount;
import com.example.banking.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionHistoryService
{

    @Autowired
    private BankAccountRepository repository;

    public List<String> getTransactions(long id) {
        BankAccount acc = repository.findById(id).orElse(null);
        return acc != null ? acc.getTransactions() : null;
    }
}
