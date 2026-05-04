package com.example.banking.services;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.entity.BankAccount;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientBalanceException;
import com.example.banking.exception.InvalidAmountException;
import com.example.banking.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

        @Autowired
        private BankAccountRepository repository;

    public BankAccountResponse createAccount(CreateAccountRequest req)
    {
        BankAccount acc = toEntity(req);
        BankAccount saved = repository.save(acc);
        return toResponse(saved);
    }

    public BankAccountResponse getAccount(long id)
    {
        BankAccount acc = repository.findById(id).orElse(null);
        return acc != null ? toResponse(acc) : null;
    }

    private BankAccountResponse toResponse(BankAccount acc)
    {
        BankAccountResponse res = new BankAccountResponse();
        res.setAccountNumber(acc.getAccountNumber());
        res.setAccountHolderName(acc.getAccountHolderName());
        res.setBalance(acc.getBalance());
        res.setTransactions(acc.getTransactions());
        return res;
    }

    public BankAccountResponse withdraw(long id, double amount)
    {

        if (amount <= 0) throw new InvalidAmountException();

        BankAccount acc = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (amount > acc.getBalance())
            throw new InsufficientBalanceException();

        acc.setBalance(acc.getBalance() - amount);
        acc.getTransactions().add("Withdrew: " + amount);

        return toResponse(repository.save(acc));
    }

    private BankAccount toEntity(CreateAccountRequest req)
    {
        BankAccount acc = new BankAccount();
        acc.setAccountHolderName(req.getAccountHolderName());
        acc.setBalance(req.getBalance());
        return acc;
    }
}

