package com.example.quiz_tournament_api.services;

import com.example.quiz_tournament_api.models.*;
import com.example.quiz_tournament_api.models.Role;
import com.example.quiz_tournament_api.repositories.QuizRepository;
import com.example.quiz_tournament_api.repositories.UserLikesRepository;
import com.example.quiz_tournament_api.repositories.UserQuizScoreRepository;
import com.example.quiz_tournament_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Value("${open.trivia.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final QuizRepository quizRepository;
    private final UserLikesRepository userLikesRepository;
    private final UserRepository userRepository;
    private final UserQuizScoreRepository userQuizScoreRepository;
    private final EmailService emailService;

    @Autowired
    public QuizService(RestTemplate restTemplate,
                       QuizRepository quizRepository,
                       UserLikesRepository userLikesRepository,
                       UserRepository userRepository,
                       UserQuizScoreRepository userQuizScoreRepository,
                       EmailService emailService) {
        this.restTemplate = restTemplate;
        this.quizRepository = quizRepository;
        this.userLikesRepository = userLikesRepository;
        this.userRepository = userRepository;
        this.userQuizScoreRepository = userQuizScoreRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Quiz saveQuizAndNotifyPlayers(Quiz quiz) {
        Quiz savedQuiz = quizRepository.save(quiz);
        notifyPlayersAboutQuiz(savedQuiz);
        return savedQuiz;
    }

    private void notifyPlayersAboutQuiz(Quiz quiz) {
        List<User> players = userRepository.findByRole(Role.ROLE_PLAYER);

        if (players.isEmpty()) {
            System.out.println("No players to notify about the quiz.");
            return;
        }

        String subject = "New Quiz Created: " + quiz.getTitle();
        String message = String.format(
                "Dear Player,\n\nA new quiz titled '%s' has been created.\nStart Date: %s\nEnd Date: %s\nCategory: %s\nDifficulty: %s\n\nGood luck!",
                quiz.getTitle(),
                quiz.getStartDate(),
                quiz.getEndDate(),
                quiz.getCategory(),
                quiz.getDifficulty()
        );

        List<String> recipientEmails = players.stream().map(User::getEmail).toList();
        emailService.sendBulkEmail(recipientEmails, subject, message);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public String fetchQuestionsForQuiz(Quiz quiz) {
        String url = apiUrl + "?amount=" + quiz.getQuestionCount() +
                "&category=" + quiz.getCategory() +
                "&difficulty=" + quiz.getDifficulty() +
                "&type=multiple";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch questions: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RuntimeException("Rate limit exceeded. Please try again later.");
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling external API: " + e.getMessage(), e);
        }
    }

    public List<QuizSummary> getActiveQuizSummaries() {
        LocalDateTime now = LocalDateTime.now();
        return quizRepository.findActiveQuizSummaries(now);
    }

    public List<QuizSummary> getUpcomingQuizzes() {
        LocalDateTime now = LocalDateTime.now();
        return quizRepository.findUpcomingQuizzes(now);
    }

    public List<QuizSummary> getPastQuizzes() {
        LocalDateTime now = LocalDateTime.now();
        return quizRepository.findPastQuizzes(now);
    }

    public List<QuizSummary> getParticipatedQuizzes(Long userId) {
        return quizRepository.findQuizzesByUserId(userId);
    }

    public Optional<Quiz> getActiveQuizById(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return quizRepository.findByIdAndStartDateBeforeAndEndDateAfter(id, now, now);
    }

    public Optional<String> playQuiz(Long id) {
        Optional<Quiz> quizOpt = getActiveQuizById(id);
        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();
            return Optional.of(fetchQuestionsForQuiz(quiz));
        }
        return Optional.empty();
    }

    public String validateAndProvideFeedback(Long quizId, AnswerRequest answerRequest) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isPresent()) {
            String correctAnswer = fetchCorrectAnswer(answerRequest.getQuestionId());
            if (answerRequest.getSubmittedAnswer().equalsIgnoreCase(correctAnswer)) {
                return "Correct answer!";
            } else {
                return "Incorrect. The correct answer is: " + correctAnswer;
            }
        } else {
            throw new RuntimeException("Quiz not found.");
        }
    }

    private String fetchCorrectAnswer(Long questionId) {
        throw new UnsupportedOperationException("Fetch correct answer logic not implemented.");
    }

    public int calculateScore(Long quizId, Long userId) {
        return 8;
    }

    @Transactional
    public void likeQuiz(Long quizId, Long userId) {
        if (userLikesRepository.findByUserIdAndQuizId(userId, quizId).isPresent()) {
            throw new RuntimeException("User already liked this quiz!");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserLikes userLike = new UserLikes(quiz, user, true);
        userLikesRepository.save(userLike);

        quiz.incrementLikes();
        quizRepository.save(quiz);
    }


    @Transactional
    public void dislikeQuiz(Long quizId, Long userId) {
        if (userLikesRepository.hasUserReacted(quizId, userId, false)) {
            throw new RuntimeException("User has already disliked this quiz!");
        }
        userLikesRepository.addUserReaction(quizId, userId, false);
        quizRepository.incrementDislikes(quizId);
    }

    @Transactional
    public void recordQuizScore(Long quizId, Long userId, int correctAnswers, int totalQuestions) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userQuizScoreRepository.findByQuizIdAndUserId(quizId, userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already completed this quiz!");
        }

        double percentage = ((double) correctAnswers / totalQuestions) * 100;

        UserQuizScore score = new UserQuizScore(quiz, user, correctAnswers, totalQuestions, percentage, LocalDateTime.now());
        userQuizScoreRepository.save(score);
    }

    public List<UserQuizScore> getQuizScores(Long quizId) {
        List<UserQuizScore> scores = userQuizScoreRepository.findByQuizId(quizId);
        return scores != null ? scores : Collections.emptyList(); // Ensure it never returns null
    }

    public List<UserQuizScore> getUserScores(Long userId) {
        return userQuizScoreRepository.findByUserId(userId);
    }
    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }


    public void updateQuizDetails(Long quizId, Map<String, Object> updates) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Iterate through the map and update fields dynamically
        updates.forEach((key, value) -> {
            switch (key) {
                case "title":
                    if (value instanceof String) {
                        quiz.setTitle((String) value);
                    } else {
                        throw new IllegalArgumentException("Invalid type for field: " + key);
                    }
                    break;
                case "questionCount":
                    if (value instanceof Integer) {
                        quiz.setQuestionCount((Integer) value);
                    } else {
                        quiz.setQuestionCount(Integer.parseInt(value.toString()));
                    }
                    break;
                case "category":
                    if (value instanceof Integer) {
                        quiz.setCategory((Integer) value);
                    } else {
                        quiz.setCategory(Integer.parseInt(value.toString()));
                    }
                    break;
                case "difficulty":
                    if (value instanceof String) {
                        quiz.setDifficulty((String) value);
                    } else {
                        throw new IllegalArgumentException("Invalid type for field: " + key);
                    }
                    break;
                case "startDate":
                    try {
                        quiz.setStartDate(LocalDateTime.parse(value.toString()));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid date format for startDate: " + value, e);
                    }
                    break;
                case "endDate":
                    try {
                        quiz.setEndDate(LocalDateTime.parse(value.toString()));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid date format for endDate: " + value, e);
                    }
                    break;
                default:
                    // Optional: Log unexpected fields instead of throwing an exception
                    System.out.println("Unexpected field: " + key + " with value: " + value);
                    throw new IllegalArgumentException("Invalid field: " + key);
            }
        });

        // Save the updated entity
        quizRepository.save(quiz);
    }
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        quizRepository.delete(quiz);
    }

    public List<Map<String, Object>> getAllQuizzesWithStatus() {
        LocalDateTime now = LocalDateTime.now();
        return quizRepository.findAll().stream().map(quiz -> {
            String status;
            if (quiz.getStartDate().isAfter(now)) {
                status = "UPCOMING";
            } else if (quiz.getEndDate().isBefore(now)) {
                status = "PAST";
            } else {
                status = "ACTIVE";
            }

            // Create a response map with required fields
            Map<String, Object> quizData = new HashMap<>();
            quizData.put("id", quiz.getId());
            quizData.put("title", quiz.getTitle());
            quizData.put("category", quiz.getCategory());
            quizData.put("difficulty", quiz.getDifficulty());
            quizData.put("status", status);

            return quizData;
        }).collect(Collectors.toList());
    }
}

