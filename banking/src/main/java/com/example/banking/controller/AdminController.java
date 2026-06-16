package com.example.banking.controller;

import com.example.banking.dto.BankAccountResponse;
import com.example.banking.dto.UserDTO;
import com.example.banking.services.AccountService;
import com.example.banking.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Operations", description = "Administrative operations for system management")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve a list of all users in the system")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/accounts")
    @Operation(summary = "Get all accounts", description = "Retrieve all bank accounts with pagination")
    public ResponseEntity<Page<BankAccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BankAccountResponse> accounts = adminService.getAllAccounts(page, size);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get system statistics", description = "Retrieve system-wide statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = adminService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/accounts/{accountNumber}/freeze")
    @Operation(summary = "Freeze account", description = "Freeze a bank account")
    public ResponseEntity<?> freezeAccount(@PathVariable Long accountNumber, @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Account frozen by admin");
            BankAccountResponse response = accountService.freezeAccount(accountNumber, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accounts/{accountNumber}/unfreeze")
    @Operation(summary = "Unfreeze account", description = "Unfreeze a frozen bank account")
    public ResponseEntity<?> unfreezeAccount(@PathVariable Long accountNumber) {
        try {
            BankAccountResponse response = accountService.unfreezeAccount(accountNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accounts/{accountNumber}/close")
    @Operation(summary = "Close account", description = "Close a bank account")
    public ResponseEntity<?> closeAccount(@PathVariable Long accountNumber) {
        try {
            BankAccountResponse response = accountService.closeAccount(accountNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-daily-limits")
    @Operation(summary = "Reset daily transfer limits", description = "Reset daily transfer limits for all accounts")
    public ResponseEntity<?> resetDailyLimits() {
        try {
            accountService.resetDailyTransferLimit();
            return ResponseEntity.ok(Map.of("message", "Daily transfer limits reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
