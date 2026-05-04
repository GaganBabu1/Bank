package com.example.banking.exception;

public class InsufficientBalanceException extends RuntimeException
{
    public InsufficientBalanceException()
    {
        super("Insufficient balance");
    }
}