package com.example.quiz_tournament_api.controllers;

import com.example.quiz_tournament_api.models.Role;
import com.example.quiz_tournament_api.models.User;
import com.example.quiz_tournament_api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.quiz_tournament_api.repositories.UserQuizScoreRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private UserQuizScoreRepository userQuizScoreRepository;
    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder,UserQuizScoreRepository userQuizScoreRepository ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userQuizScoreRepository = userQuizScoreRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<Object> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            userService.generatePasswordResetToken(email);
            return ResponseEntity.ok("Password reset token sent to your email.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        // Check if username already exists
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        // Check if email already exists
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        // Proceed with creating the user if validations pass
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(null); // Respond with 400 Bad Request for password update attempts
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // Respond with 404 if user not found
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{quizId}/check-completion")
    public ResponseEntity<Map<String, Boolean>> checkQuizCompletion(
            @PathVariable Long quizId,
            @RequestParam Long userId) {
        boolean isCompleted = userQuizScoreRepository.findByQuizIdAndUserId(quizId, userId).isPresent();
        return ResponseEntity.ok(Map.of("completed", isCompleted));
    }


    @PostMapping("/auth/login")
    public ResponseEntity<Object> login(@RequestBody User loginRequest) {
        // Fetch the user from the database
        User user = userService.getUserByUsername(loginRequest.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if the password matches
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // Determine the user's role
            Role userRole = user.getRole();

            // Log the matched role for debugging
            System.out.println("User role: " + userRole);

            // Return the role, ID, and a success message
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "role", userRole.name(), // Convert enum to string
                    "id", user.getId() // Include the user ID
            ));
        } else {
            // Debugging: Log mismatch
            System.out.println("Password did not match for user: " + user.getUsername());
            System.out.println("Raw password: " + loginRequest.getPassword());
            System.out.println("Encoded password from DB: " + user.getPassword());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
