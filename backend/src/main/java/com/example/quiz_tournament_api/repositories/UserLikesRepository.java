package com.example.quiz_tournament_api.repositories;

import com.example.quiz_tournament_api.models.UserLikes;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserLikesRepository extends JpaRepository<UserLikes, Long> {

    // Add a user's reaction (like or dislike)
    @Modifying
    @Transactional
    @Query("INSERT INTO UserLikes(quiz, user, liked) SELECT q, u, :liked FROM Quiz q, User u WHERE q.id = :quizId AND u.id = :userId")
    void addUserReaction(Long quizId, Long userId, boolean liked);

    // Check if a user has reacted (liked or disliked) to a quiz
    @Query("SELECT COUNT(ul) > 0 FROM UserLikes ul WHERE ul.quiz.id = :quizId AND ul.user.id = :userId AND ul.liked = :liked")
    boolean hasUserReacted(Long quizId, Long userId, boolean liked);

    // Remove a user's reaction (like or dislike) from a quiz
    @Modifying
    @Transactional
    @Query("DELETE FROM UserLikes ul WHERE ul.quiz.id = :quizId AND ul.user.id = :userId")
    void removeUserReaction(Long quizId, Long userId);

    // Find a UserLikes record by user and quiz
    Optional<UserLikes> findByUserIdAndQuizId(Long userId, Long quizId);
}
