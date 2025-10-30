package com.cognizant.userservice.controller;

import java.util.List;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cognizant.userservice.model.User;
import com.cognizant.userservice.service.UserService;
import com.cognizant.userservice.dto.UserProfileUpdateDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing user-related operations.
 * Provides endpoints for retrieving, updating, registering, and deleting user accounts.
 */
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Retrieves a list of all users.
     * This endpoint is typically for administrative use.
     *
     * @return A ResponseEntity containing a list of all User objects and an HTTP status of OK.
     */
    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        log.info("Received request to find all users");
        List<User> users = userService.findAll();
        log.info("Found {} users", users.size());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id The unique identifier of the user to retrieve.
     * @return A ResponseEntity containing the User object if found, and an HTTP status of OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        log.info("Received request to find user by id: {}", id);
        User user = userService.findById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Retrieves the details of the currently authenticated user.
     * This is useful for populating user profile sections.
     *
     * @param principal The Principal object representing the currently authenticated user.
     * @return A ResponseEntity containing the User object of the current user and an HTTP status of OK.
     * Returns UNAUTHORIZED if the principal is null or its name is not available.
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserDetails(Principal principal) {
        log.info("Received request to get current user details for principal: {}", principal.getName());
        // Check if the principal is available
        if (principal == null || principal.getName() == null) {
            log.warn("Principal is null, cannot get current user details");
            // In a real application, Spring Security usually handles this before reaching the controller.
            // Returning UNAUTHORIZED as a fallback if it somehow bypasses security filters.
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // Retrieve user details based on the authenticated principal's username
        User user = userService.findByUsername(principal.getName());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Updates the profile of the currently authenticated user.
     * Allows users to modify their own profile information.
     *
     * @param principal The Principal object representing the currently authenticated user.
     * @param updateDTO A DTO containing the fields to be updated in the user's profile.
     * @return A ResponseEntity containing the updated User object and an HTTP status of OK.
     * Returns UNAUTHORIZED if the principal is null or its name is not available.
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUserProfile(Principal principal, @RequestBody UserProfileUpdateDTO updateDTO) {
        log.info("Received request to update current user profile for principal: {}", principal.getName());
        // Check if the principal is available
        if (principal == null || principal.getName() == null) {
            log.warn("Principal is null, cannot update current user profile");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // Update the user profile using the service layer
        User updatedUser = userService.updateUserProfile(principal.getName(), updateDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    /**
     * Registers a new user.
     *
     * @param user The User object containing the details of the user to be registered.
     * @return A ResponseEntity containing the newly created User object and an HTTP status of CREATED.
     */
    @PostMapping("/register")
    public ResponseEntity<User> save(@RequestBody User user) {
        log.info("Received request to register new user with username: {}", user.getUsername());
        User savedUser = userService.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    /**
     * Deletes a user by their unique ID.
     * This endpoint is typically for administrative use.
     *
     * @param id The unique identifier of the user to delete.
     * @return A ResponseEntity with no content and an HTTP status of NoContent.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Received request to delete user with id: {}", id);
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * **NEW ENDPOINT for Admin to update any user's profile**
     * Updates the profile of a specific user identified by their ID.
     * This endpoint requires ADMIN role and the admin's authorization token.
     *
     * @param id The unique identifier of the user whose profile is to be updated.
     * @param updateDTO A DTO containing the fields to be updated in the user's profile.
     * @return A ResponseEntity containing the updated User object and an HTTP status of OK.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Ensure only ADMINs can access this
    public ResponseEntity<User> updateAnyUserProfile(@PathVariable Long id, @RequestBody UserProfileUpdateDTO updateDTO) {
        log.info("Admin received request to update user profile for id: {}", id);
        User updatedUser = userService.updateUserProfileById(id, updateDTO); // Call a new service method
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
}