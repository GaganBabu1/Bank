package com.example.banking.services;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.entity.BankAccount;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientBalanceException;
import com.example.banking.exception.InvalidAmountException;
import com.example.banking.repository.BankAccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService
{

    @Autowired
    private BankAccountRepository repository;

    // Deposit
    public BankAccountResponse deposit(long id, double amount)
    {

        if (amount <= 0) throw new InvalidAmountException();

        BankAccount acc = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        acc.setBalance(acc.getBalance() + amount);
        acc.getTransactions().add("Deposited: " + amount);

        return toResponse(repository.save(acc));
    }

    // Withdraw
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

    // Transfer
    @Transactional
    public BankAccountResponse transfer(long from, long to, double amount)
    {

        if (from == to) throw new InvalidAmountException();
        if (amount <= 0) throw new InvalidAmountException();

        BankAccount sender = repository.findById(from)
                .orElseThrow(() -> new AccountNotFoundException(from));

        BankAccount receiver = repository.findById(to)
                .orElseThrow(() -> new AccountNotFoundException(to));

        if (amount > sender.getBalance())
            throw new InsufficientBalanceException();

        sender.setBalance(sender.getBalance() - amount);
        sender.getTransactions().add("Transferred " + amount + " to " + to);

        receiver.setBalance(receiver.getBalance() + amount);
        receiver.getTransactions().add("Received " + amount + " from " + from);

        repository.save(sender);
        repository.save(receiver);

        return toResponse(sender);
    }

    // Mapping (Entity → DTO)
    private BankAccountResponse toResponse(BankAccount acc)
    {
        BankAccountResponse res = new BankAccountResponse();
        res.setAccountNumber(acc.getAccountNumber());
        res.setAccountHolderName(acc.getAccountHolderName());
        res.setBalance(acc.getBalance());
        return res;
    }
}