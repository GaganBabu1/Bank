package com.example.banking.services;

import com.example.banking.dto.TransactionDTO;
import com.example.banking.entity.Transaction;
import com.example.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TransactionHistoryService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Page<TransactionDTO> getTransactionHistory(Long accountNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> transactions = transactionRepository.findTransactionsByAccount(accountNumber, pageable);
        return transactions.map(this::toDTO);
    }

    public List<TransactionDTO> getAllTransactionsForAccount(Long accountNumber) {
        Pageable pageable = PageRequest.of(0, 1000, Sort.by("createdAt").descending());
        Page<Transaction> transactions = transactionRepository.findTransactionsByAccount(accountNumber, pageable);
        return transactions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionByRef(String transactionRef) {
        Transaction transaction = transactionRepository.findByTransactionRef(transactionRef)
                .orElse(null);
        return transaction != null ? toDTO(transaction) : null;
    }

    private TransactionDTO toDTO(Transaction transaction) {
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
