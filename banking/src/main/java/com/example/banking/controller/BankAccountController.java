package com.example.banking.controller;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.dto.TransactionDTO;
import com.example.banking.dto.TransactionRequest;
import com.example.banking.dto.TransferRequest;
import com.example.banking.services.AccountService;
import com.example.banking.services.TransactionHistoryService;
import com.example.banking.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Banking Operations", description = "Account and transaction management")
public class BankAccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionHistoryService historyService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new bank account", description = "Create a new bank account for the authenticated user")
    public ResponseEntity<BankAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest req, Authentication authentication) {
        try {
            // Extract userId from JWT (stored in details)
            Long userId = (Long) authentication.getDetails();
            if (userId == null || userId == 0) {
                // If userId not in token details, throw error
                return ResponseEntity.badRequest().build();
            }
            
            BankAccountResponse response = accountService.createAccount(req, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get account details", description = "Retrieve details of a specific bank account")
    public ResponseEntity<BankAccountResponse> getAccount(@PathVariable long id) {
        BankAccountResponse res = accountService.getAccount(id);
        return res != null
                ? ResponseEntity.ok(res)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get user's accounts", description = "Retrieve all accounts for a specific user")
    public ResponseEntity<List<BankAccountResponse>> getUserAccounts(@PathVariable Long userId) {
        List<BankAccountResponse> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Deposit funds", description = "Deposit money into an account")
    public ResponseEntity<?> deposit(@Valid @RequestBody TransactionRequest req) {
        try {
            BankAccountResponse acc = transactionService.deposit(
                    req.getAccountNumber(),
                    req.getAmount()
            );
            return ResponseEntity.ok(acc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Withdraw funds", description = "Withdraw money from an account")
    public ResponseEntity<?> withdraw(@Valid @RequestBody TransactionRequest req) {
        try {
            BankAccountResponse acc = transactionService.withdraw(req.getAccountNumber(), req.getAmount());
            return ResponseEntity.ok(acc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Transfer funds", description = "Transfer money between two accounts")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest req) {
        try {
            BankAccountResponse acc = transactionService.transfer(
                    req.getFromAccount(),
                    req.getToAccount(),
                    req.getAmount()
            );
            return ResponseEntity.ok(acc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get transaction history", description = "Retrieve paginated transaction history for an account")
    public ResponseEntity<?> getTransactions(
            @PathVariable long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TransactionDTO> transactions = transactionService.getAccountTransactions(id, page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/transactions/outgoing")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get outgoing transactions", description = "Retrieve outgoing transactions for an account")
    public ResponseEntity<?> getOutgoingTransactions(
            @PathVariable long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TransactionDTO> transactions = transactionService.getOutgoingTransactions(id, page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/transactions/incoming")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get incoming transactions", description = "Retrieve incoming transactions for an account")
    public ResponseEntity<?> getIncomingTransactions(
            @PathVariable long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TransactionDTO> transactions = transactionService.getIncomingTransactions(id, page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transaction/{transactionRef}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get transaction details", description = "Retrieve details of a specific transaction by reference number")
    public ResponseEntity<?> getTransactionDetails(@PathVariable String transactionRef) {
        try {
            TransactionDTO transaction = historyService.getTransactionByRef(transactionRef);
            if (transaction != null) {
                return ResponseEntity.ok(transaction);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}







