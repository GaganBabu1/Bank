package com.example.banking.services;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.User;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientBalanceException;
import com.example.banking.exception.InvalidAmountException;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public BankAccountResponse createAccount(CreateAccountRequest req, Long userId) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount acc = new BankAccount();
        acc.setUser(user);
        acc.setAccountHolderName(req.getAccountHolderName());
        acc.setBalance(req.getBalance());
        acc.setDailyTransferLimit(100000.0); // Default limit
        acc.setDailyTransferUsed(0.0);
        acc.setAccountStatus(BankAccount.AccountStatus.ACTIVE);
        acc.setCreatedAt(LocalDateTime.now());
        acc.setUpdatedAt(LocalDateTime.now());

        BankAccount saved = accountRepository.save(acc);
        return toResponse(saved);
    }

    public BankAccountResponse getAccount(long id) {
        BankAccount acc = accountRepository.findById(id).orElse(null);
        return acc != null ? toResponse(acc) : null;
    }

    public List<BankAccountResponse> getUserAccounts(Long userId) {
        List<BankAccount> accounts = accountRepository.findByUserId(userId);
        return accounts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<BankAccountResponse> getAllActiveAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BankAccount> accounts = accountRepository.findAllActiveAccounts(pageable);
        return accounts.map(this::toResponse);
    }

    public BankAccountResponse freezeAccount(Long accountNumber, String reason) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        account.setAccountStatus(BankAccount.AccountStatus.FROZEN);
        account.setFreezeDate(LocalDateTime.now());
        account.setFreezeReason(reason);
        account.setUpdatedAt(LocalDateTime.now());

        BankAccount saved = accountRepository.save(account);
        return toResponse(saved);
    }

    public BankAccountResponse unfreezeAccount(Long accountNumber) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        account.setAccountStatus(BankAccount.AccountStatus.ACTIVE);
        account.setFreezeDate(null);
        account.setFreezeReason(null);
        account.setUpdatedAt(LocalDateTime.now());

        BankAccount saved = accountRepository.save(account);
        return toResponse(saved);
    }

    public BankAccountResponse closeAccount(Long accountNumber) {
        BankAccount account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (account.getBalance() > 0) {
            throw new RuntimeException("Cannot close account with remaining balance. Please withdraw funds first.");
        }

        account.setAccountStatus(BankAccount.AccountStatus.CLOSED);
        account.setUpdatedAt(LocalDateTime.now());

        BankAccount saved = accountRepository.save(account);
        return toResponse(saved);
    }

    public void resetDailyTransferLimit() {
        List<BankAccount> accounts = accountRepository.findAll();
        for (BankAccount account : accounts) {
            account.setDailyTransferUsed(0.0);
        }
        accountRepository.saveAll(accounts);
    }

    private BankAccountResponse toResponse(BankAccount acc) {
        return new BankAccountResponse(
                acc.getAccountNumber(),
                acc.getUser() != null ? acc.getUser().getId() : null,
                acc.getAccountHolderName(),
                acc.getBalance(),
                acc.getDailyTransferLimit(),
                acc.getDailyTransferUsed(),
                acc.getAccountStatus().toString(),
                acc.getCreatedAt(),
                acc.getUpdatedAt(),
                acc.getFreezeDate(),
                acc.getFreezeReason()
        );
    }

    public BankAccountResponse withdraw(long id, double amount) {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than 0");

        BankAccount acc = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (amount > acc.getBalance())
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");

        acc.setBalance(acc.getBalance() - amount);
        acc.setUpdatedAt(LocalDateTime.now());

        return toResponse(accountRepository.save(acc));
    }
}

