package com.example.banking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;



@RestController
@CrossOrigin(origins = "http://localhost:5173")



public class BankAccountController {
    @Autowired
    private BankAccountRepository repository;
    private long nextAccountNumber = 1001;

    /*post*/
    @PostMapping("/accounts")
    public BankAccount createAccount(@RequestBody BankAccount account) {
        return repository.save(account);
    }

    /*get*/
    @GetMapping("/accounts/{id}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/accounts/deposit")
    public ResponseEntity<BankAccount> deposit(@RequestBody TransactionRequest request) {

        return repository.findById(request.getAccountNumber())
                .map(account -> {
                    account.setBalance(account.getBalance() + request.getAmount());
                    account.getTransactions().add("Deposited: " + request.getAmount());
                    return ResponseEntity.ok(repository.save(account));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/accounts/withdraw")
    public ResponseEntity<BankAccount> withdraw(@RequestBody TransactionRequest request) {

        BankAccount account = repository.findById(request.getAccountNumber()).orElse(null);

        if (account == null) {
            return ResponseEntity.<BankAccount>notFound().build();
        }

        if (request.getAmount() > account.getBalance()) {
            return ResponseEntity.badRequest().body(null);
        }

        account.setBalance(account.getBalance() - request.getAmount());
        account.getTransactions().add("Withdrew: " + request.getAmount());

        return ResponseEntity.ok(repository.save(account));
    }


    @PostMapping("/accounts/transfer")
    public ResponseEntity<BankAccount> transfer(@RequestBody TransferRequest request) {

        BankAccount sender = repository.findById(request.getFromAccount()).orElse(null);
        BankAccount receiver = repository.findById(request.getToAccount()).orElse(null);

        if (sender == null || receiver == null) {
            return ResponseEntity.<BankAccount>notFound().build();
        }

        if (sender.getAccountNumber() == receiver.getAccountNumber()) {
            return ResponseEntity.badRequest().body(null);
        }

        if (request.getAmount() > sender.getBalance()) {
            return ResponseEntity.badRequest().body(null);
        }

        sender.setBalance(sender.getBalance() - request.getAmount());
        sender.getTransactions().add(
                "Transferred " + request.getAmount() + " to " + receiver.getAccountNumber()
        );

        receiver.setBalance(receiver.getBalance() + request.getAmount());
        receiver.getTransactions().add(
                "Received " + request.getAmount() + " from " + sender.getAccountNumber()
        );

        repository.save(sender);
        repository.save(receiver);

        return ResponseEntity.ok(sender);
    }

    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<List<String>> getTransactions(@PathVariable long accountNumber) {

        return repository.findById(accountNumber)
                .map(account -> ResponseEntity.ok(account.getTransactions()))
                .orElse(ResponseEntity.notFound().build());
    }
}







