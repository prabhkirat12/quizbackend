package com.example.quiz_tournament_api.repositories;

import com.example.quiz_tournament_api.models.User;
import com.example.quiz_tournament_api.models.Role; // Correct import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find a user by ID and role
    Optional<User> findByIdAndRole(Long id, Role role);

    // Find users by role
    List<User> findByRole(Role role);
}
