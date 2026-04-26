package com.investresearch.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    // email and phone excluded — re-verification required, handled via Firebase directly
    private String country;
    private String province;
    private String ageRange;
    private String profession;
}
