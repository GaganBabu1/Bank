package com.example.banking.controller;

import com.example.banking.dto.RegisterRequest;
import com.example.banking.dto.UpdateProfileRequest;
import com.example.banking.dto.UserDTO;
import com.example.banking.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "User Management", description = "User profile and account management")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get user profile", description = "Retrieve user profile information")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable Long userId) {
        UserDTO userDTO = userService.getUserProfile(userId);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/profile/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Update user profile", description = "Update user profile information")
    public ResponseEntity<UserDTO> updateUserProfile(@PathVariable Long userId, @Valid @RequestBody UpdateProfileRequest request) {
        UserDTO userDTO = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(userDTO);
    }
}
