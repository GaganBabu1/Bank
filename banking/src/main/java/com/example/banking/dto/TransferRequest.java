package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferRequest {
    @NotNull(message = "Sender account is required")
    private long fromAccount;

    @NotNull(message = "Receiver account is required")
    private long toAccount;

    @Positive(message = "Amount must be greater than 0")
    private double amount;

    public long getFromAccount()
    {
        return fromAccount;
    }

    public void setFromAccount(long fromAccount)
    {
        this.fromAccount = fromAccount;
    }

    public long getToAccount()
    {
        return toAccount;
    }

    public void setToAccount(long toAccount)
    {
        this.toAccount = toAccount;
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
