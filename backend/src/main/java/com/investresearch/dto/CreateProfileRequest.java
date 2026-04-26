package com.investresearch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProfileRequest {

    @NotBlank private String email;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank private String phone;

    // Optional fields — shown as easy dropdowns on signup
    private String country;
    private String province;
    private String ageRange;
    private String profession;
}
