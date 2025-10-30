package com.cognizant.userservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for updating a user's profile information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO {

    private String firstName;

    private String lastName;

    private String email;

    private Long mobile;
} 