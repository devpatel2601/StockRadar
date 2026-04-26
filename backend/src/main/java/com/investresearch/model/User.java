package com.investresearch.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String firebaseUid;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String phone;

    @Builder.Default
    private String country = "Canada";

    private String province;
    private String ageRange;
    private String profession;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
