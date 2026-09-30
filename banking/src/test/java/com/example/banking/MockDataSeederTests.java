package com.example.banking;

import com.example.banking.config.MockDataSeeder.MockDataInitializer;
import com.example.banking.entity.BankAccount;
import com.example.banking.entity.User;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MockDataSeederTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsMockUserAccountAndTransactionsWhenProfileRuns() {
        when(passwordEncoder.encode("Mock@12345")).thenReturn("encoded-password");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            var user = invocation.getArgument(0, com.example.banking.entity.User.class);
            user.setId(42L);
            return user;
        });
        when(accountRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            BankAccount account = invocation.getArgument(0, BankAccount.class);
            account.setAccountNumber(1001L);
            return account;
        });
        when(accountRepository.findByUserId(42L)).thenReturn(List.of());

        new MockDataInitializer(userRepository, accountRepository, transactionRepository, passwordEncoder).run();

        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(accountRepository).saveAndFlush(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getBalance()).isEqualTo(47000.0);
        assertThat(accountCaptor.getValue().getAccountStatus()).isEqualTo(BankAccount.AccountStatus.ACTIVE);
        verify(transactionRepository, org.mockito.Mockito.times(4)).save(any());
    }

    @Test
    void doesNothingWhenMockUserAlreadyExists() {
        User existingUser = new User();
        existingUser.setId(42L);
        when(userRepository.findByEmail("mock.user@banking.local")).thenReturn(Optional.of(existingUser));
        when(accountRepository.findByUserId(42L)).thenReturn(List.of(new BankAccount()));

        new MockDataInitializer(userRepository, accountRepository, transactionRepository, passwordEncoder).run();

        verify(userRepository, never()).save(any());
        verify(accountRepository, never()).saveAndFlush(any());
        verify(transactionRepository, never()).save(any());
    }
}
