package com.example.banking.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(long id) {
        super("Account not found: " + id);
    }

    public AccountNotFoundException(String identifier) {
        super("Account or resource not found: " + identifier);
    }
}