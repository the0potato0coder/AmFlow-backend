package com.cognizant.userservice.model;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A custom implementation of {@link UserDetails} that wraps a {@link User} entity.
 * This class is used by Spring Security to obtain authentication and authorization
 * information about the user.
 */
public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final User user;

    /**
     * Constructs a UserPrincipal with the specified User.
     *
     * @param user the user entity to be wrapped
     */
    public UserPrincipal(User user) {
        this.user = user;
    }

    /**
     * Returns the authorities granted to the user.
     * In this implementation, a single authority is granted based on the user's role.
     *
     * @return a collection containing the user's granted authority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Grant authority based on the user's role.
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return the user's password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the username used to authenticate the user.
     * Here, the actual username field is used.
     *
     * @return the user's username
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Indicates whether the user's account has expired.
     * An expired account cannot be authenticated.
     *
     * @return {@code true} if the user's account is valid (non-expired), {@code false} otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * A locked user cannot be authenticated.
     *
     * @return {@code true} if the user is not locked, {@code false} otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     * Expired credentials prevent authentication.
     *
     * @return {@code true} if the user's credentials are valid (non-expired), {@code false} otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * A disabled user cannot be authenticated.
     *
     * @return {@code true} if the user is enabled, {@code false} otherwise
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
