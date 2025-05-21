package com.example.examapp.model;

import com.example.examapp.utils.QuestionStatus;

public class QuestionModel {
    private String questionId;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int correctOption;
    private int selectedOption;
    private int status;
    private boolean isBookMarked;
    private String categoryName;
    private String testId;

    public QuestionModel() {
        this.selectedOption = -1;
        this.status = QuestionStatus.NOT_VISITED;
        this.isBookMarked = false;
    }

    public QuestionModel(String categoryName, String testId, String questionId, String question,
                         String optionA, String optionB, String optionC, String optionD,
                         int correctOption, int selectedOption, int status, boolean isBookMarked) {
        this.categoryName = categoryName;
        this.testId = testId;
        this.questionId = questionId;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.selectedOption = selectedOption;
        this.status = status;
        this.isBookMarked = isBookMarked;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public int getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(int correctOption) {
        this.correctOption = correctOption;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        this.selectedOption = selectedOption;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isBookMarked() {
        return isBookMarked;
    }

    public void setBookMarked(boolean bookMarked) {
        isBookMarked = bookMarked;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }
}