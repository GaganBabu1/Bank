package com.example.banking.services;

import com.example.banking.dto.MoneyHealthResponse;
import com.example.banking.dto.LowBalancePredictionResponse;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AiInsightService {

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

        public LowBalancePredictionResponse getLowBalancePrediction(Long userId) {
        double threshold = 2000.0;
            if (userId == null) {
                return prediction(0.0, 0.0, 0, threshold, false,
                        "There is not enough outgoing history to predict a low balance.",
                        List.of("No authenticated account history was found"));
            }

            List<BankAccount> activeAccounts = accountRepository.findByUserId(userId).stream()
            .filter(account -> account.getAccountStatus() == BankAccount.AccountStatus.ACTIVE)
            .toList();
        double currentBalance = activeAccounts.stream()
            .mapToDouble(account -> valueOrZero(account.getBalance()))
            .sum();

            if (activeAccounts.isEmpty()) {
                return prediction(0.0, 0.0, 0, threshold, false,
                    "There is not enough outgoing history to predict a low balance.",
                    List.of("No active account history was found"));
            }

            if (currentBalance <= threshold) {
                return prediction(currentBalance, currentBalance, 0, threshold, true,
                    "Your balance is already at or below the safety threshold.",
                    List.of("Current balance is below the recommended safety threshold"));
            }

        Map<Long, Transaction> transactionsById = new LinkedHashMap<>();
        for (BankAccount account : activeAccounts) {
            transactionRepository.findTransactionsByAccount(account.getAccountNumber(), Pageable.unpaged())
                .forEach(transaction -> transactionsById.put(transaction.getId(), transaction));
        }

        List<Transaction> outgoingTransactions = transactionsById.values().stream()
            .filter(this::isOutgoing)
            .toList();
        double outgoingTotal = outgoingTransactions.stream()
            .mapToDouble(transaction -> valueOrZero(transaction.getAmount()))
            .sum();

        if (outgoingTransactions.isEmpty() || outgoingTotal <= 0) {
            return prediction(currentBalance, currentBalance, 0, threshold, false,
                "There is not enough outgoing history to predict a low balance.",
                List.of("No recent withdrawals or outgoing transfers were found"));
        }

        LocalDateTime earliestTransaction = outgoingTransactions.stream()
            .map(Transaction::getCreatedAt)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());
        long observedDays = Math.max(1, ChronoUnit.DAYS.between(earliestTransaction, LocalDateTime.now()));
        double dailyOutgoingRate = outgoingTotal / observedDays;
        int estimatedDays = (int) Math.ceil((currentBalance - threshold) / dailyOutgoingRate);
        double projectedBalance = Math.max(0, currentBalance - (dailyOutgoingRate * estimatedDays));
        boolean warning = projectedBalance <= threshold;

        return prediction(currentBalance, projectedBalance, estimatedDays, threshold, warning,
            "Based on recent outgoing activity, your balance may fall below " + formatAmount(threshold)
                + " within " + estimatedDays + " days.",
            List.of(
                "Average outgoing activity is " + formatAmount(dailyOutgoingRate) + " per day",
                "The projection uses " + outgoingTransactions.size() + " outgoing transaction(s)"
            ));
        }

        private LowBalancePredictionResponse prediction(
            double currentBalance,
            double projectedBalance,
            int estimatedDays,
            double threshold,
            boolean warning,
            String message,
            List<String> reasons) {
        return new LowBalancePredictionResponse(
            warning, estimatedDays, currentBalance, projectedBalance, threshold, message, reasons);
        }

        private String formatAmount(double amount) {
        return String.format("%.0f", amount);
        }

    public MoneyHealthResponse getMoneyHealth(Long userId) {
    if (userId == null) {
        return noHistoryResponse();
    }

    List<BankAccount> accounts = accountRepository.findByUserId(userId);
    List<BankAccount> activeAccounts = accounts.stream()
        .filter(account -> account.getAccountStatus() == BankAccount.AccountStatus.ACTIVE)
        .toList();

    Map<Long, Transaction> transactionsById = new LinkedHashMap<>();
    for (BankAccount account : accounts) {
        Page<Transaction> transactions = transactionRepository.findTransactionsByAccount(
            account.getAccountNumber(), Pageable.unpaged());
        transactions.forEach(transaction -> transactionsById.put(transaction.getId(), transaction));
    }

    if (activeAccounts.isEmpty() && transactionsById.isEmpty()) {
        return noHistoryResponse();
    }

    double totalBalance = activeAccounts.stream()
        .mapToDouble(account -> valueOrZero(account.getBalance()))
        .sum();
    double incoming = transactionsById.values().stream()
        .filter(this::isIncoming)
        .mapToDouble(transaction -> valueOrZero(transaction.getAmount()))
        .sum();
    double outgoing = transactionsById.values().stream()
        .filter(this::isOutgoing)
        .mapToDouble(transaction -> valueOrZero(transaction.getAmount()))
        .sum();
    long withdrawals = transactionsById.values().stream()
        .filter(transaction -> transaction.getType() == Transaction.TransactionType.WITHDRAW)
        .count();
    double transferUsage = activeAccounts.stream()
        .mapToDouble(account -> percentage(account.getDailyTransferUsed(), account.getDailyTransferLimit()))
        .average()
        .orElse(0.0);

    int score = 50;
    if (totalBalance > 0) score += 15;
    if (incoming >= outgoing) score += 15;
    else score -= 10;
    if (transferUsage < 25) score += 10;
    else if (transferUsage < 60) score += 5;
    else score -= 5;
    if (withdrawals <= 3) score += 5;
    else score -= 5;

    score = Math.max(0, Math.min(100, score));
    List<String> reasons = List.of(
        incoming >= outgoing ? "Incoming activity covers outgoing activity" : "Outgoing activity is higher than incoming activity",
        transferUsage < 25 ? "Transfer-limit usage is low" : "Transfer-limit usage is elevated",
        withdrawals <= 3 ? "Withdrawal activity is limited" : "Withdrawal activity is frequent"
    );

    return new MoneyHealthResponse(score, statusFor(score), reasons);
    }

    private MoneyHealthResponse noHistoryResponse() {
    return new MoneyHealthResponse(
        50,
        "NEEDS_ATTENTION",
        List.of("Create an active account to receive personalized money health insights")
    );
    }

    private boolean isIncoming(Transaction transaction) {
    return transaction.getType() == Transaction.TransactionType.DEPOSIT
        || transaction.getType() == Transaction.TransactionType.TRANSFER_IN;
    }

    private boolean isOutgoing(Transaction transaction) {
    return transaction.getType() == Transaction.TransactionType.WITHDRAW
        || transaction.getType() == Transaction.TransactionType.TRANSFER_OUT;
    }

    private double percentage(Double value, Double limit) {
    if (limit == null || limit <= 0) return 100.0;
    return valueOrZero(value) / limit * 100;
    }

    private double valueOrZero(Double value) {
    return value == null ? 0.0 : value;
    }

    private String statusFor(int score) {
    if (score >= 70) return "HEALTHY";
    if (score >= 40) return "WATCH";
    return "AT_RISK";
    }
}
