package com.cognizant.userservice.service;

import com.cognizant.userservice.model.User;
import com.cognizant.userservice.model.UserPrincipal;
import com.cognizant.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Custom implementation of the {@link UserDetailsService} interface.
 * This service is responsible for retrieving user details required for authentication,
 * wrapping the user data inside a {@link UserPrincipal} which encapsulates the necessary
 * information such as password and granted authorities.
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads the user details for Spring Security based on the provided username.
     * <p>
     * This method retrieves the {@link User} entity from the {@link UserRepository} and,
     * if found, wraps it inside a {@link UserPrincipal} which implements {@link UserDetails}.
     * If the user is not found, a {@link UsernameNotFoundException} is thrown.
     * </p>
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated {@link UserPrincipal} object
     * @throws UsernameNotFoundException if no user is found for the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        return new UserPrincipal(userOptional.get());
    }
}
