package com.example._Do.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entity class representing a User in the database.
 * <p>
 * This class maps to the 'users' table and implements Spring Security's UserDetails interface.
 * By implementing UserDetails directly, this entity serves as the principal object
 * in the security context, allowing access to user credentials and authorities.
 * </p>
 */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @Size(min = 2, max = 20, message = "Last name must be between 2 and 20 characters")
    @NotBlank(message = "Last name is required")
    private String lastName;

    /**
     * User's email address. Used as the unique username for authentication.
     */
    @Column(name = "email", unique = true, nullable = false)
    @Size(max = 50, message = "Email is too long")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    /**
     * The encrypted password.
     * Never store plain-text passwords here.
     */
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * The role assigned to the user.
     * Stored as a String in the database (e.g., 'ROLE_USER').
     */
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role is required")
    private Role role;

    // --- UserDetails Interface Implementation ---

    /**
     * Returns the authorities granted to the user.
     * Delegates the authority generation to the {@link Role} enum logic.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    /**
     * Returns the username used to authenticate the user.
     * We use the email address as the username.
     */
    @Override
    public String getUsername() {
        return this.email;
    }

    /**
     * Returns the password used to authenticate the user.
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    // Account status flags (Can be customized for logic like "Ban User" or "Verify Email")

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}