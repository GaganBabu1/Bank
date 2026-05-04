package com.example.banking.dto;

import java.util.List;

public class BankAccountResponse
{
    private long accountNumber;
    private String accountHolderName;
    private double balance;
    private List<String> transactions;

    public long getAccountNumber()
    {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber)
    {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName()
    {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName)
    {
        this.accountHolderName = accountHolderName;
    }

    public double getBalance()
    {
        return balance;
    }

    public void setBalance(double balance)
    {
        this.balance = balance;
    }

    public List<String> getTransactions()
    {
        return transactions;
    }

    public void setTransactions(List<String> transactions)
    {
        this.transactions = transactions;
    }
}