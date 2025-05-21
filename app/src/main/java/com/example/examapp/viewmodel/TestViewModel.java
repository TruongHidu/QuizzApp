package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.CategoryModel;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.model.TestModel;
import com.example.examapp.repository.AuthRepository;
import com.example.examapp.utils.QuestionStatus;

import java.util.List;
import java.util.function.Consumer;

public class TestViewModel extends ViewModel {
    private AuthRepository authRepository;

    public TestViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<List<TestModel>> getTests() {
        return authRepository.getTests();
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }

    public void loadTestData() {
        authRepository.loadTestData();
    }

    public void loadCategories(MyCompleteListener listener) {
        authRepository.loadCategories(listener);
    }

    public void loadQuestions(MyCompleteListener listener) {
        authRepository.loadQuestions(listener);
    }

    public List<TestModel> getCurrentTestList() {
        return authRepository.getCurrentTestList();
    }

    public List<CategoryModel> getCurrentCategoryList() {
        return authRepository.getCurrentCategoryList();
    }

    public int getSelectedTestIndex() {
        return authRepository.getSelectedTestIndex();
    }

    public int getSelectedCategoryIndex() {
        return authRepository.getSelectedCategoryIndex();
    }

    public List<QuestionModel> getCurrentQuestionList() {
        return authRepository.getCurrentQuestionList();
    }

    public void updateBookmark(int questionIndex, boolean isBookmarked) {
        authRepository.updateBookmark(questionIndex, isBookmarked);
    }

    public void clearSelection(int questionIndex) {
        QuestionModel question = authRepository.getCurrentQuestionList().get(questionIndex);
        question.setSelectedOption(-1);
        question.setStatus(QuestionStatus.UNANSWERED);
    }

    public void markForReview(int questionIndex, boolean isMarked) {
        QuestionModel question = authRepository.getCurrentQuestionList().get(questionIndex);
        if (isMarked) {
            question.setStatus(QuestionStatus.HIGHTLIGHTED);
        } else {
            question.setStatus(question.getSelectedOption() != -1 ? QuestionStatus.ANSWERED : QuestionStatus.UNANSWERED);
        }
    }

    public void calculateAndSaveScore(long timeTaken, Consumer<AuthRepository.ScoreResult> onScoreCalculated, MyCompleteListener onSaveComplete) {
        AuthRepository.ScoreResult scoreResult = authRepository.calculateScore();
        authRepository.updateBookmarks();
        authRepository.saveTestResult(scoreResult.finalScore, onSaveComplete);
        onScoreCalculated.accept(scoreResult);
    }

    public void resetQuestionsForReAttempt() {
        for (QuestionModel question : authRepository.getCurrentQuestionList()) {
            question.setSelectedOption(-1);
            question.setStatus(QuestionStatus.NOT_VISITED);
        }
    }

    public void setSelectedTestIndex(int index) {
        authRepository.setSelectedTestIndex(index);
    }
}