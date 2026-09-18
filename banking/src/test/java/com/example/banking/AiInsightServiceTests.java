package com.example.banking;

import com.example.banking.dto.MoneyHealthResponse;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.services.AiInsightService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiInsightServiceTests {

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AiInsightService aiInsightService;

    @Test
    void calculatesHealthyScoreFromLiveAccountAndTransactionData() {
        BankAccount account = account(10L, 1000.0, 100000.0, 1000.0);
        Transaction deposit = transaction(1L, Transaction.TransactionType.DEPOSIT, 500.0);
        Transaction withdrawal = transaction(2L, Transaction.TransactionType.WITHDRAW, 100.0);
        when(accountRepository.findByUserId(42L)).thenReturn(List.of(account));
        when(transactionRepository.findTransactionsByAccount(10L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(deposit, withdrawal)));

        MoneyHealthResponse result = aiInsightService.getMoneyHealth(42L);

        assertThat(result.score()).isEqualTo(95);
        assertThat(result.status()).isEqualTo("HEALTHY");
        assertThat(result.reasons()).contains("Incoming activity covers outgoing activity");
        verify(accountRepository).findByUserId(42L);
        verify(transactionRepository).findTransactionsByAccount(10L, Pageable.unpaged());
    }

    @Test
    void marksHighOutgoingAndLimitUsageAsAtRisk() {
        BankAccount account = account(20L, 0.0, 100.0, 80.0);
        List<Transaction> transactions = List.of(
                transaction(3L, Transaction.TransactionType.DEPOSIT, 100.0),
                transaction(4L, Transaction.TransactionType.TRANSFER_OUT, 500.0),
                transaction(5L, Transaction.TransactionType.WITHDRAW, 10.0),
                transaction(6L, Transaction.TransactionType.WITHDRAW, 10.0),
                transaction(7L, Transaction.TransactionType.WITHDRAW, 10.0),
                transaction(8L, Transaction.TransactionType.WITHDRAW, 10.0)
        );
        when(accountRepository.findByUserId(43L)).thenReturn(List.of(account));
        when(transactionRepository.findTransactionsByAccount(20L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(transactions));

        MoneyHealthResponse result = aiInsightService.getMoneyHealth(43L);

        assertThat(result.score()).isEqualTo(30);
        assertThat(result.status()).isEqualTo("AT_RISK");
        assertThat(result.reasons()).contains("Outgoing activity is higher than incoming activity");
    }

    @Test
    void returnsSafeFallbackWhenUserHasNoAccounts() {
        when(accountRepository.findByUserId(44L)).thenReturn(List.of());

        MoneyHealthResponse result = aiInsightService.getMoneyHealth(44L);

        assertThat(result.score()).isEqualTo(50);
        assertThat(result.status()).isEqualTo("NEEDS_ATTENTION");
        assertThat(result.reasons()).containsExactly(
                "Create an active account to receive personalized money health insights");
        verifyNoInteractions(transactionRepository);
    }

    private BankAccount account(Long number, double balance, double limit, double used) {
        BankAccount account = new BankAccount();
        account.setAccountNumber(number);
        account.setBalance(balance);
        account.setDailyTransferLimit(limit);
        account.setDailyTransferUsed(used);
        account.setAccountStatus(BankAccount.AccountStatus.ACTIVE);
        return account;
    }

    private Transaction transaction(Long id, Transaction.TransactionType type, double amount) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setType(type);
        transaction.setAmount(amount);
        return transaction;
    }
}
