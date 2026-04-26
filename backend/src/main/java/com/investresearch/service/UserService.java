package com.investresearch.service;

import com.investresearch.dto.CreateProfileRequest;
import com.investresearch.dto.UpdateProfileRequest;
import com.investresearch.model.User;
import com.investresearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByUid(String uid) {
        return userRepository.findById(uid);
    }

    public User createProfile(String uid, CreateProfileRequest req) {
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An account with this phone number already exists.");
        }
        User user = User.builder()
                .firebaseUid(uid)
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .phone(req.getPhone())
                .country(req.getCountry() != null ? req.getCountry() : "Canada")
                .province(req.getProvince())
                .ageRange(req.getAgeRange())
                .profession(req.getProfession())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    public User updateProfile(String uid, UpdateProfileRequest req) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        if (req.getFirstName()  != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()   != null) user.setLastName(req.getLastName());
        if (req.getCountry()    != null) user.setCountry(req.getCountry());
        if (req.getProvince()   != null) user.setProvince(req.getProvince());
        if (req.getAgeRange()   != null) user.setAgeRange(req.getAgeRange());
        if (req.getProfession() != null) user.setProfession(req.getProfession());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
