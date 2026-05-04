package com.example.banking.controller;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.services.AccountService;
import com.example.banking.services.TransactionHistoryService;
import com.example.banking.dto.TransactionRequest;
import com.example.banking.dto.TransferRequest;
import com.example.banking.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


import java.util.List;



@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BankAccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionHistoryService historyService;

    @PostMapping("/accounts")
    public BankAccountResponse createAccount(@Valid @RequestBody CreateAccountRequest req) {
        return accountService.createAccount(req);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<BankAccountResponse> getAccount(@PathVariable long id) {
        BankAccountResponse res = accountService.getAccount(id);

        return res != null
                ? ResponseEntity.ok(res)
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/accounts/deposit")
    public ResponseEntity<BankAccountResponse> deposit(@RequestBody TransactionRequest req) {

        BankAccountResponse acc = transactionService.deposit(
                req.getAccountNumber(),
                req.getAmount()
        );

        return ResponseEntity.ok(acc);
    }

    @PostMapping("/accounts/withdraw")
    public ResponseEntity<BankAccountResponse> withdraw(@Valid @RequestBody TransactionRequest req) {

        BankAccountResponse acc =
                transactionService.withdraw(req.getAccountNumber(), req.getAmount());

        return ResponseEntity.ok(acc);
    }

    @PostMapping("/accounts/transfer")
    public ResponseEntity<BankAccountResponse> transfer(@Valid @RequestBody TransferRequest req) {

        BankAccountResponse acc = transactionService.transfer(
                req.getFromAccount(),
                req.getToAccount(),
                req.getAmount()
        );

        return ResponseEntity.ok(acc);
    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<String>> getTransactions(@PathVariable long id) {
        List<String> tx = historyService.getTransactions(id);
        return tx != null
                ? ResponseEntity.ok(tx)
                : ResponseEntity.notFound().build();
    }
}







