package com.example.quiz_tournament_api.controllers;

import com.example.quiz_tournament_api.models.Quiz;
import com.example.quiz_tournament_api.models.QuizSummary;
import com.example.quiz_tournament_api.models.AnswerRequest;
import com.example.quiz_tournament_api.models.UserQuizScore;
import com.example.quiz_tournament_api.services.EmailService;
import com.example.quiz_tournament_api.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createQuiz(@RequestBody Quiz quiz) {
        quizService.saveQuizAndNotifyPlayers(quiz);
        return ResponseEntity.ok("Quiz created successfully! Notifications sent to players.");
    }

    @PutMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateQuizDetails(
            @PathVariable Long quizId,
            @RequestBody Map<String, Object> updates
    ) {
        quizService.updateQuizDetails(quizId, updates);
        return ResponseEntity.ok("Quiz details updated successfully!");
    }


    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok("Quiz deleted successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Quiz not found.");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/all-with-status")
    public ResponseEntity<List<Map<String, Object>>> getAllQuizzesWithStatus() {
        List<Map<String, Object>> quizzesWithStatus = quizService.getAllQuizzesWithStatus();
        return ResponseEntity.ok(quizzesWithStatus);
    }

    @GetMapping("/{quizId}/participants")
    public ResponseEntity<List<UserQuizScore>> getQuizParticipants(@PathVariable Long quizId) {
        List<UserQuizScore> participants = quizService.getQuizScores(quizId);
        return ResponseEntity.ok(participants);
    }


    @GetMapping("/{id}/play")
    @PreAuthorize("hasAnyRole('PLAYER')")
    public ResponseEntity<String> playQuiz(@PathVariable Long id) {
        Optional<Quiz> quiz = quizService.getActiveQuizById(id);
        if (quiz.isPresent()) {
            String questions = quizService.fetchQuestionsForQuiz(quiz.get());
            return ResponseEntity.ok(questions);
        } else {
            return ResponseEntity.status(403).body("Quiz is not available for play or has expired.");
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submitAnswer(@PathVariable Long id, @RequestBody AnswerRequest answerRequest) {
        String feedback = quizService.validateAndProvideFeedback(id, answerRequest);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/{id}/score")
    public ResponseEntity<String> getQuizScore(@PathVariable Long id, @RequestParam Long userId) {
        int score = quizService.calculateScore(id, userId);
        return ResponseEntity.ok("Your score: " + score + "/10");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> fetchCategories() {
        String apiUrl = "https://opentdb.com/api_category.php";
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);
            List<Map<String, Object>> categories = (List<Map<String, Object>>) response.getBody().get("trivia_categories");
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<String> likeQuiz(@PathVariable Long id, @RequestParam Long userId) {
        quizService.likeQuiz(id, userId);
        return ResponseEntity.ok("Quiz liked successfully!");
    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<String> unlikeQuiz(@PathVariable Long id, @RequestParam Long userId) {
        quizService.dislikeQuiz(id, userId);
        return ResponseEntity.ok("Quiz unliked successfully!");
    }


    @PostMapping("/{quizId}/score")
    public ResponseEntity<String> recordScore(
            @PathVariable Long quizId,
            @RequestParam Long userId,
            @RequestParam int correctAnswers,
            @RequestParam int totalQuestions
    ) {
        quizService.recordQuizScore(quizId, userId, correctAnswers, totalQuestions);
        return ResponseEntity.ok("Score recorded successfully!");
    }

    @GetMapping("/{quizId}/scores")
    public ResponseEntity<List<UserQuizScore>> getQuizScores(@PathVariable Long quizId) {
        List<UserQuizScore> scores = quizService.getQuizScores(quizId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/user/{userId}/scores")
    public ResponseEntity<List<UserQuizScore>> getUserScores(@PathVariable Long userId) {
        List<UserQuizScore> scores = quizService.getUserScores(userId);
        return ResponseEntity.ok(scores);
    }
}

