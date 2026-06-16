package com.example.banking.services;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.dto.UserDTO;
import com.example.banking.entity.BankAccount;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getAddress(),
                        user.getRole().toString(),
                        user.getEnabled(),
                        user.getCreatedAt(),
                        user.getLastLogin()
                ))
                .collect(Collectors.toList());
    }

    public Page<BankAccountResponse> getAllAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BankAccount> accounts = bankAccountRepository.findAll(pageable);
        return accounts.map(account -> new BankAccountResponse(
                account.getAccountNumber(),
                account.getUser() != null ? account.getUser().getId() : null,
                account.getAccountHolderName(),
                account.getBalance(),
                account.getDailyTransferLimit(),
                account.getDailyTransferUsed(),
                account.getAccountStatus().toString(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                account.getFreezeDate(),
                account.getFreezeReason()
        ));
    }

    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalAccounts = bankAccountRepository.getTotalAccountCount();
        Double totalBalance = bankAccountRepository.getTotalActiveBalance();
        Long totalUsers = (long) userRepository.findAll().size();

        stats.put("totalAccounts", totalAccounts);
        stats.put("totalActiveBalance", totalBalance != null ? totalBalance : 0.0);
        stats.put("totalUsers", totalUsers);
        stats.put("activeAccounts", bankAccountRepository.findAllActiveAccounts(PageRequest.of(0, 1000)).getTotalElements());
        stats.put("frozenAccounts", bankAccountRepository.findAllFrozenAccounts(PageRequest.of(0, 1000)).getTotalElements());

        return stats;
    }

    public BankAccountResponse freezeAccount(Long accountNumber, String reason) {
        AccountService accountService = new AccountService();
        return null; // Implemented in AccountService
    }

    public BankAccountResponse unfreezeAccount(Long accountNumber) {
        AccountService accountService = new AccountService();
        return null; // Implemented in AccountService
    }
}
