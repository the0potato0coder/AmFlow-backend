package com.cognizant.userservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.userservice.exception.DataAccessException;
import com.cognizant.userservice.exception.UserNotFoundException;
import com.cognizant.userservice.exception.UsernameAlreadyExistsException;
import com.cognizant.userservice.model.User;
import com.cognizant.userservice.model.User.Role;
import com.cognizant.userservice.repository.UserRepository;
import com.cognizant.userservice.repository.LeaveRepository;
import com.cognizant.userservice.repository.AttendanceRepository;
import com.cognizant.userservice.repository.AttendanceAdjustmentRepository;
import com.cognizant.userservice.dto.UserProfileUpdateDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceAdjustmentRepository attendanceAdjustmentRepository;

    // PasswordEncoder can be autowired or instantiated as a bean in a config class
    // For simplicity, keeping it here for now, but autowiring is generally preferred.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Retrieves all users from the database.
     *
     * @return A list of all {@link User} entities.
     * @throws DataAccessException if an error occurs during data retrieval.
     */
    public List<User> findAll() {
        log.info("Finding all users");
        // For simple findAll, let Spring Data JPA exceptions propagate.
        // A global @ControllerAdvice can catch DataAccessException or specific JPA exceptions.
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The {@link User} entity if found.
     * @throws UserNotFoundException if no user is found with the given ID.
     */
    public User findById(Long id) {
        log.info("Finding user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for id: {}", id);
                    return new UserNotFoundException(id);
                });
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve.
     * @return The {@link User} entity if found.
     * @throws UserNotFoundException if no user is found with the given username.
     */
    public User findByUsername(String username) {
        log.info("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User with username '{}' not found.", username);
                    return new UserNotFoundException(String.format("User with username '%s' not found", username));
                });
    }

    /**
     * Updates the profile information for an existing user by username.
     * This is typically used for a user updating their own profile.
     *
     * @param username The username of the user whose profile is to be updated.
     * @param updateDTO The DTO containing the updated profile information.
     * @return The updated {@link User} entity.
     * @throws UserNotFoundException if the user to update is not found.
     */
    public User updateUserProfile(String username, UserProfileUpdateDTO updateDTO) {
        log.info("Updating profile for user: {}", username);

        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Update failed: User with username '{}' not found.", username);
                    return new UserNotFoundException(String.format("User with username '%s' not found for update", username));
                });

        return applyProfileUpdatesAndSave(existingUser, updateDTO);
    }

    /**
     * Saves a new user to the database. Encodes the password before saving.
     * Assigns 'EMPLOYEE' role by default if not specified.
     *
     * @param user The {@link User} entity to save.
     * @return The saved {@link User} entity with generated ID and encoded password.
     * @throws UsernameAlreadyExistsException if a user with the given username already exists.
     */
    public User save(User user) {
        log.info("Saving new user with username: {}", user.getUsername());

        // Check if username already exists; throws UsernameAlreadyExistsException if true.
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            log.warn("Save failed: Username '{}' already exists.", user.getUsername());
            throw new UsernameAlreadyExistsException(user.getUsername());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default role if not provided
        if (user.getRole() == null) {
            user.setRole(Role.EMPLOYEE);
        }

        // Save the user. Persistence exceptions will naturally propagate.
        User savedUser = userRepository.save(user);
        log.info("User saved successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Deletes a user and all their associated data (leaves, attendance, adjustments) by ID.
     * This operation is transactional.
     *
     * @param id The ID of the user to delete.
     * @throws UserNotFoundException if the user to delete is not found.
     * @throws DataAccessException if an error occurs during deletion of associated data or the user itself.
     */
    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting user with id: {}", id);

        // Check if user exists before attempting deletion.
        if (!userRepository.existsById(id)) {
            log.warn("Deletion failed: User with id {} not found for deletion.", id);
            throw new UserNotFoundException(id);
        }

        // Deleting associated data. These operations are part of the transaction.
        // If any of these fail, the transaction will roll back due to @Transactional
        // and an unchecked exception will propagate.
        log.debug("Deleting leave records for user: {}", id);
        leaveRepository.deleteByUserId(id); // Assuming this method exists and works as expected
        log.debug("Deleting attendance adjustment records for user: {}", id);
        attendanceAdjustmentRepository.deleteByUserId(id); // Assuming this method exists and works as expected
        log.debug("Deleting attendance records for user: {}", id);
        attendanceRepository.deleteByUserId(id); // Assuming this method exists and works as expected

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    /**
     * **NEW METHOD for Admin to update any user's profile by ID.**
     * Updates the profile information for an existing user by their ID.
     *
     * @param id The ID of the user whose profile is to be updated.
     * @param updateDTO The DTO containing the updated profile information.
     * @return The updated {@link User} entity.
     * @throws UserNotFoundException if the user to update is not found.
     */
    public User updateUserProfileById(Long id, UserProfileUpdateDTO updateDTO) {
        log.info("Updating profile for user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: User with ID '{}' not found.", id);
                    return new UserNotFoundException(id);
                });

        return applyProfileUpdatesAndSave(existingUser, updateDTO);
    }

    // Helper method to apply updates and save, reducing code duplication
    private User applyProfileUpdatesAndSave(User user, UserProfileUpdateDTO updateDTO) {
        if (updateDTO.getFirstName() != null) {
            user.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            user.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getMobile() != null) {
            user.setMobile(updateDTO.getMobile());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", user.getUsername() != null ? user.getUsername() : user.getId());
        return updatedUser;
    }

}