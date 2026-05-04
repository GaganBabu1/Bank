package com.example.banking.Model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "bank_account")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountNumber;

    private String accountHolderName;
    private double balance;

    @ElementCollection
    @CollectionTable(name = "transactions", joinColumns = @JoinColumn(name = "account_number"))
    @Column(name = "transaction")
    private List<String> transactions = new ArrayList<>();

    public BankAccount() {
    }

    public long getAccountNumber() {

        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {

        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {

        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {

        this.accountHolderName = accountHolderName;
    }

    public double getBalance() {

        return balance;
    }

    public void setBalance(double balance) {

        this.balance = balance;
    }

    public List<String> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<String> transactions) {

        this.transactions = transactions;
    }
}