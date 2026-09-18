package com.example.banking.config;

import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.entity.User;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("mock-data")
public class MockDataSeeder {

    private static final String MOCK_EMAIL = "mock.user@banking.local";

    @Bean
    public MockDataInitializer mockDataInitializer(
            UserRepository userRepository,
            BankAccountRepository accountRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder) {
        return new MockDataInitializer(
                userRepository,
                accountRepository,
                transactionRepository,
                passwordEncoder
        );
    }

    public static class MockDataInitializer implements org.springframework.boot.CommandLineRunner {

        private final UserRepository userRepository;
        private final BankAccountRepository accountRepository;
        private final TransactionRepository transactionRepository;
        private final PasswordEncoder passwordEncoder;

        public MockDataInitializer(
                UserRepository userRepository,
                BankAccountRepository accountRepository,
                TransactionRepository transactionRepository,
                PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.accountRepository = accountRepository;
            this.transactionRepository = transactionRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        public void run(String... args) {
            User savedUser = userRepository.findByEmail(MOCK_EMAIL).orElseGet(() -> {
                User user = new User();
                user.setFirstName("Mock");
                user.setLastName("User");
                user.setEmail(MOCK_EMAIL);
                user.setPassword(passwordEncoder.encode("Mock@12345"));
                user.setRole(User.Role.USER);
                user.setEnabled(true);
                user.setAccountNonLocked(true);
                user.setPhoneNumber("9999999999");
                user.setAddress("Local testing account");
                return userRepository.save(user);
            });

            List<BankAccount> existingAccounts = accountRepository.findByUserId(savedUser.getId());
            if (!existingAccounts.isEmpty()) {
                return;
            }

            BankAccount account = new BankAccount();
            account.setUser(savedUser);
            account.setAccountHolderName("Mock User");
            account.setBalance(47000.0);
            account.setDailyTransferLimit(100000.0);
            account.setDailyTransferUsed(15000.0);
            account.setAccountStatus(BankAccount.AccountStatus.ACTIVE);
            BankAccount savedAccount = accountRepository.saveAndFlush(account);

            LocalDateTime now = LocalDateTime.now();
            saveTransaction(transactionRepository, savedAccount, null, 50000.0,
                    Transaction.TransactionType.DEPOSIT, "Mock salary deposit", 0.0, 50000.0, now.minusDays(8));
            saveTransaction(transactionRepository, savedAccount, savedAccount, 5000.0,
                    Transaction.TransactionType.WITHDRAW, "Mock cash withdrawal", 50000.0, 45000.0, now.minusDays(5));
            saveTransaction(transactionRepository, savedAccount, savedAccount, 10000.0,
                    Transaction.TransactionType.TRANSFER_IN, "Mock incoming transfer", 45000.0, 55000.0, now.minusDays(3));
            saveTransaction(transactionRepository, savedAccount, savedAccount, 8000.0,
                    Transaction.TransactionType.TRANSFER_OUT, "Mock outgoing transfer", 55000.0, 47000.0, now.minusDays(1));
        }

        private void saveTransaction(
                TransactionRepository transactionRepository,
                BankAccount fromAccount,
                BankAccount toAccount,
                double amount,
                Transaction.TransactionType type,
                String description,
                double balanceBefore,
                double balanceAfter,
                LocalDateTime createdAt) {
            Transaction transaction = new Transaction();
            transaction.setFromAccount(type == Transaction.TransactionType.DEPOSIT ? null : fromAccount);
            transaction.setToAccount(type == Transaction.TransactionType.WITHDRAW ? null : toAccount);
            transaction.setAmount(amount);
            transaction.setType(type);
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            transaction.setDescription(description);
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(balanceAfter);
            transaction.setCreatedAt(createdAt);
            transactionRepository.save(transaction);
        }
    }
}
