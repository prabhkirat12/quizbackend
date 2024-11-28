package com.example.quiz_tournament_api.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // Category of the question

    @Column(nullable = false)
    private String type; // Question type (e.g., multiple, boolean)

    @Column(nullable = false)
    private String difficulty; // Difficulty level (easy, medium, hard)

    @Column(nullable = false, length = 1000) // Accommodate longer question text
    private String text; // The actual question text

    @Column(nullable = false)
    private String correctAnswer; // Correct answer for the question

    @ElementCollection
    @CollectionTable(name = "question_incorrect_answers", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "incorrect_answer")
    private List<String> incorrectAnswers; // List of incorrect answers

    @Transient
    private List<String> options; // Combined list of correct and incorrect answers (not persisted)

    @Transient
    private String submittedAnswer; // The user's submitted answer (not persisted)

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz; // Reference to the parent quiz

    // Constructors
    public Question() {}

    public Question(String category, String type, String difficulty, String text, String correctAnswer, List<String> incorrectAnswers) {
        this.category = category;
        this.type = type;
        this.difficulty = difficulty;
        this.text = text;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
        this.options = generateOptions(); // Generate options (correct + incorrect)
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
        this.options = generateOptions(); // Re-generate options whenever incorrectAnswers changes
    }

    public List<String> getOptions() {
        if (options == null) {
            options = generateOptions(); // Generate options if not already generated
        }
        return options;
    }

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public void setSubmittedAnswer(String submittedAnswer) {
        this.submittedAnswer = submittedAnswer;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    // Utility Methods

    /**
     * Combines the correct answer and incorrect answers into a single list, shuffles them, and returns the result.
     *
     * @return a shuffled list of all answer options.
     */
    private List<String> generateOptions() {
        List<String> allOptions = new ArrayList<>();
        if (correctAnswer != null) {
            allOptions.add(correctAnswer);
        }
        if (incorrectAnswers != null) {
            allOptions.addAll(incorrectAnswers);
        }
        Collections.shuffle(allOptions); // Shuffle the options to randomize order
        return allOptions;
    }

    /**
     * Validates the submitted answer against the correct answer.
     *
     * @return true if the submitted answer matches the correct answer, false otherwise.
     */
    public boolean isAnswerCorrect() {
        return correctAnswer != null && correctAnswer.equals(submittedAnswer);
    }
}
