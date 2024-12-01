package com.example.quiz_tournament_api.repositories;

import com.example.quiz_tournament_api.models.UserQuizScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuizScoreRepository extends JpaRepository<UserQuizScore, Long> {
    List<UserQuizScore> findByQuizId(Long quizId);

    List<UserQuizScore> findByUserId(Long userId);

    Optional<UserQuizScore> findByQuizIdAndUserId(Long quizId, Long userId);
}


