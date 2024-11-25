package com.example.quiz_tournament_api.services;

import com.example.quiz_tournament_api.models.User;
import com.example.quiz_tournament_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.quiz_tournament_api.models.Role;
import java.util.UUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Map<String, String> resetTokenStore = new HashMap<>();

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    // Check if a username exists
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // Check if an email exists
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // Create a new user (with password hashing)
    public User createUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_PLAYER); // Set default role if none provided
        }
        return userRepository.save(user);
    }

    // Update user
    public User updateUser(Long id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Update fields only if they are non-null
                    if (user.getUsername() != null)
                        existingUser.setUsername(user.getUsername());
                    if (user.getEmail() != null)
                        existingUser.setEmail(user.getEmail());
                    if (user.getFirstName() != null)
                        existingUser.setFirstName(user.getFirstName());
                    if (user.getLastName() != null)
                        existingUser.setLastName(user.getLastName());
                    if (user.getRole() != null)
                        existingUser.setRole(user.getRole());
                    if (user.getAge() != null)
                        existingUser.setAge(user.getAge());

                    // Ignore password updates
                    if (user.getPassword() != null) {
                        throw new UnsupportedOperationException("Password updates are not allowed.");
                    }

                    // Save and return the updated user
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }


    // Delete user
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with this email not found"));

        // Generate a unique token
        String token = UUID.randomUUID().toString();

        // Store the token and associated email
        resetTokenStore.put(token, email);

        // Send email with the reset token
        sendResetTokenEmail(email, token);

        System.out.println("Password reset token for " + email + ": " + token); // For debugging purposes
    }

    private void sendResetTokenEmail(String email, String token) {
        String subject = "Password Reset Request";
        String message = "Hello,\n\n" +
                "We received a request to reset your password. Click the link below to reset your password:\n\n" +
                "http://localhost:8080/reset-password?token=" + token + "\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Quiz Tournament Team";

        // Send the email
        emailService.sendBulkEmail(List.of(email), subject, message);
    }

    public void resetPassword(String token, String newPassword) {
        String email = resetTokenStore.get(token);
        if (email == null) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with this email not found"));

        user.setPassword(passwordEncoder.encode(newPassword)); // Hash the new password
        userRepository.save(user); // Save the updated user

        resetTokenStore.remove(token); // Invalidate the token after use
    }
}

