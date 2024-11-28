package com.example.quiz_tournament_api.repositories;

import com.example.quiz_tournament_api.models.Quiz;
import com.example.quiz_tournament_api.models.QuizSummary;
import com.example.quiz_tournament_api.models.UserLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Method to find active quizzes as summaries
    @Query("SELECT new com.example.quiz_tournament_api.models.QuizSummary(q.id, q.title, 'ACTIVE') " +
            "FROM Quiz q WHERE q.startDate <= :now AND q.endDate >= :now")
    List<QuizSummary> findActiveQuizSummaries(LocalDateTime now);

    // Method to find upcoming quizzes as summaries
    @Query("SELECT new com.example.quiz_tournament_api.models.QuizSummary(q.id, q.title, 'UPCOMING') " +
            "FROM Quiz q WHERE q.startDate > :now")
    List<QuizSummary> findUpcomingQuizzes(LocalDateTime now);

    // Method to find past quizzes as summaries
    @Query("SELECT new com.example.quiz_tournament_api.models.QuizSummary(q.id, q.title, 'PAST') " +
            "FROM Quiz q WHERE q.endDate < :now")
    List<QuizSummary> findPastQuizzes(LocalDateTime now);

    // Method to find quizzes participated by a specific user
    @Query("SELECT new com.example.quiz_tournament_api.models.QuizSummary(q.id, q.title, 'PARTICIPATED') " +
            "FROM Quiz q JOIN q.participants p WHERE p.id = :userId")
    List<QuizSummary> findQuizzesByUserId(Long userId);

    // Method to find a quiz by ID if it's active
    Optional<Quiz> findByIdAndStartDateBeforeAndEndDateAfter(Long id, LocalDateTime start, LocalDateTime end);

    // Method to increment likes for a quiz
    @Modifying
    @Transactional
    @Query("UPDATE Quiz q SET q.likes = q.likes + 1 WHERE q.id = :quizId")
    int incrementLikes(Long quizId);

    // Method to decrement likes for a quiz
    @Modifying
    @Transactional
    @Query("UPDATE Quiz q SET q.likes = q.likes - 1 WHERE q.id = :quizId AND q.likes > 0")
    int decrementLikes(Long quizId);

    // Check if a user has liked a quiz
    @Query("SELECT COUNT(ul) > 0 FROM UserLikes ul WHERE ul.quiz.id = :quizId AND ul.user.id = :userId")
    boolean hasUserLikedQuiz(Long quizId, Long userId);

    // Add a user's like to a quiz
    @Modifying
    @Transactional
    @Query("INSERT INTO UserLikes(quiz, user) SELECT q, u FROM Quiz q, User u WHERE q.id = :quizId AND u.id = :userId")
    void addUserLike(Long quizId, Long userId);

    // Remove a user's like from a quiz
    @Modifying
    @Transactional
    @Query("DELETE FROM UserLikes ul WHERE ul.quiz.id = :quizId AND ul.user.id = :userId")
    void removeUserLike(Long quizId, Long userId);

    // Method to increment dislikes for a quiz
    @Modifying
    @Transactional
    @Query("UPDATE Quiz q SET q.dislikes = q.dislikes + 1 WHERE q.id = :quizId")
    int incrementDislikes(Long quizId);

    // Method to decrement dislikes for a quiz
    @Modifying
    @Transactional
    @Query("UPDATE Quiz q SET q.dislikes = q.dislikes - 1 WHERE q.id = :quizId AND q.dislikes > 0")
    int decrementDislikes(Long quizId);

    // Check if a user has disliked a quiz
    @Query("SELECT COUNT(ul) > 0 FROM UserLikes ul WHERE ul.quiz.id = :quizId AND ul.user.id = :userId AND ul.liked = false")
    boolean hasUserDislikedQuiz(Long quizId, Long userId);


}
