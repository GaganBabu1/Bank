package com.example.banking.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransactionRequest
{


    @NotNull(message = "Account number is required")
    private long accountNumber;

    @Positive(message = "Amount must be greater than 0")
    private double amount;

    public long getAccountNumber()
    {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber)
    {
        this.accountNumber = accountNumber;
    }

    public double getAmount()
    {
        return amount;
    }

    public void setAmount(double amount)
    {
        this.amount = amount;
    }




}
