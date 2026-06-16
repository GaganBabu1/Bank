package com.example.banking.services;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.dto.TransactionDTO;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientBalanceException;
import com.example.banking.exception.InvalidAmountException;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;

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
public class TransactionService {

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Deposit
    public BankAccountResponse deposit(long id, double amount) {

        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than 0");

        BankAccount acc = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (acc.getAccountStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        Double balanceBefore = acc.getBalance();
        acc.setBalance(acc.getBalance() + amount);
        acc.setUpdatedAt(LocalDateTime.now());

        BankAccount saved = accountRepository.save(acc);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(null);
        transaction.setToAccount(acc);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setDescription("Deposit of ₹" + amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(acc.getBalance());
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return toResponse(saved);
    }

    // Withdraw
    public BankAccountResponse withdraw(long id, double amount) {

        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than 0");

        BankAccount acc = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (acc.getAccountStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        if (amount > acc.getBalance())
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");

        Double balanceBefore = acc.getBalance();
        acc.setBalance(acc.getBalance() - amount);
        acc.setUpdatedAt(LocalDateTime.now());

        BankAccount saved = accountRepository.save(acc);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(acc);
        transaction.setToAccount(null);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.WITHDRAW);
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setDescription("Withdrawal of ₹" + amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(acc.getBalance());
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return toResponse(saved);
    }

    // Transfer with daily limit checks
    public BankAccountResponse transfer(long from, long to, double amount) {

        if (from == to) throw new InvalidAmountException("Cannot transfer to same account");
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than 0");

        BankAccount sender = accountRepository.findById(from)
                .orElseThrow(() -> new AccountNotFoundException(from));

        BankAccount receiver = accountRepository.findById(to)
                .orElseThrow(() -> new AccountNotFoundException(to));

        if (sender.getAccountStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new RuntimeException("Sender account is not active");
        }

        if (receiver.getAccountStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new RuntimeException("Receiver account is not active");
        }

        if (amount > sender.getBalance())
            throw new InsufficientBalanceException("Insufficient balance for transfer");

        // Check daily transfer limit
        if (sender.getDailyTransferUsed() + amount > sender.getDailyTransferLimit()) {
            throw new RuntimeException("Daily transfer limit exceeded. Limit: ₹" + sender.getDailyTransferLimit() + 
                    ", Used: ₹" + sender.getDailyTransferUsed());
        }

        Double senderBalanceBefore = sender.getBalance();
        Double receiverBalanceBefore = receiver.getBalance();

        sender.setBalance(sender.getBalance() - amount);
        sender.setDailyTransferUsed(sender.getDailyTransferUsed() + amount);
        sender.setUpdatedAt(LocalDateTime.now());

        receiver.setBalance(receiver.getBalance() + amount);
        receiver.setUpdatedAt(LocalDateTime.now());

        BankAccount savedSender = accountRepository.save(sender);
        BankAccount savedReceiver = accountRepository.save(receiver);

        // Create outgoing transaction record
        Transaction outgoingTransaction = new Transaction();
        outgoingTransaction.setFromAccount(savedSender);
        outgoingTransaction.setToAccount(savedReceiver);
        outgoingTransaction.setAmount(amount);
        outgoingTransaction.setType(Transaction.TransactionType.TRANSFER_OUT);
        outgoingTransaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        outgoingTransaction.setDescription("Transfer to account " + to);
        outgoingTransaction.setBalanceBefore(senderBalanceBefore);
        outgoingTransaction.setBalanceAfter(savedSender.getBalance());
        outgoingTransaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(outgoingTransaction);

        // Create incoming transaction record
        Transaction incomingTransaction = new Transaction();
        incomingTransaction.setFromAccount(savedSender);
        incomingTransaction.setToAccount(savedReceiver);
        incomingTransaction.setAmount(amount);
        incomingTransaction.setType(Transaction.TransactionType.TRANSFER_IN);
        incomingTransaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        incomingTransaction.setDescription("Transfer from account " + from);
        incomingTransaction.setBalanceBefore(receiverBalanceBefore);
        incomingTransaction.setBalanceAfter(savedReceiver.getBalance());
        incomingTransaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(incomingTransaction);

        return toResponse(savedSender);
    }

    public Page<TransactionDTO> getAccountTransactions(Long accountNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findTransactionsByAccount(accountNumber, pageable);
        return transactions.map(this::toTransactionDTO);
    }

    public Page<TransactionDTO> getOutgoingTransactions(Long accountNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findOutgoingTransactions(accountNumber, pageable);
        return transactions.map(this::toTransactionDTO);
    }

    public Page<TransactionDTO> getIncomingTransactions(Long accountNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findIncomingTransactions(accountNumber, pageable);
        return transactions.map(this::toTransactionDTO);
    }

    // Mapping (Entity → DTO)
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

    private TransactionDTO toTransactionDTO(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getTransactionRef(),
                transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null,
                transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null,
                transaction.getAmount(),
                transaction.getType().toString(),
                transaction.getStatus().toString(),
                transaction.getCreatedAt(),
                transaction.getDescription(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter()
        );
    }
}