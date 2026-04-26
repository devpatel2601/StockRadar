package com.investresearch.controller;

import com.investresearch.dto.CreateProfileRequest;
import com.investresearch.dto.UpdateProfileRequest;
import com.investresearch.model.User;
import com.investresearch.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getMe() {
        return userService.findByUid(uid())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/profile")
    public ResponseEntity<User> createProfile(@Valid @RequestBody CreateProfileRequest req) {
        return ResponseEntity.ok(userService.createProfile(uid(), req));
    }

    @PatchMapping("/me")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(uid(), req));
    }

    private String uid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
