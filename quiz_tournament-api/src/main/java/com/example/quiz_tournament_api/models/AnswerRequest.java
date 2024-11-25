package com.example.quiz_tournament_api.models;

public class AnswerRequest {
    private Long questionId;      // ID of the question being answered
    private String submittedAnswer; // The user's submitted answer
    private String correctAnswer;   // The correct answer to the question

    // Default Constructor
    public AnswerRequest() {}

    // Parameterized Constructor
    public AnswerRequest(Long questionId, String submittedAnswer, String correctAnswer) {
        this.questionId = questionId;
        this.submittedAnswer = submittedAnswer;
        this.correctAnswer = correctAnswer;
    }

    // Getters and Setters
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public void setSubmittedAnswer(String submittedAnswer) {
        this.submittedAnswer = submittedAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
