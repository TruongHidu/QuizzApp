package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.repository.AuthRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookMarkViewModel extends ViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<List<QuestionModel>> bookmarkList = new MutableLiveData<>();

    public BookMarkViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<List<QuestionModel>> getBookmarkList() {
        return bookmarkList;
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }

    public void loadBookMarks(MyCompleteListener listener) {
        authRepository.loadBmIds(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                authRepository.loadBookMarks(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Get the bookmark list from AuthRepository
                        List<QuestionModel> bookmarks = authRepository.getBookmarkList();
                        // Create a new sorted list
                        List<QuestionModel> sortedBookmarks = new ArrayList<>(bookmarks);
                        // Sort by testId
                        Collections.sort(sortedBookmarks, new Comparator<QuestionModel>() {
                            @Override
                            public int compare(QuestionModel q1, QuestionModel q2) {
                                String testId1 = q1.getTestId() != null ? q1.getTestId() : "";
                                String testId2 = q2.getTestId() != null ? q2.getTestId() : "";
                                return testId1.compareTo(testId2);
                            }
                        });
                        // Post the sorted list
                        bookmarkList.postValue(sortedBookmarks);
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailture() {
                        listener.onFailture();
                    }
                });
            }

            @Override
            public void onFailture() {
                listener.onFailture();
            }
        });
    }

    public void updateBookmark(int bookmarkIndex, boolean isBookmarked) {
        List<QuestionModel> currentBookmarks = bookmarkList.getValue();
        if (currentBookmarks != null && bookmarkIndex >= 0 && bookmarkIndex < currentBookmarks.size()) {
            String questionId = currentBookmarks.get(bookmarkIndex).getQuestionId();
            authRepository.unbookmarkByQuestionId(questionId);
            // Update with sorted list
            List<QuestionModel> bookmarks = authRepository.getBookmarkList();
            List<QuestionModel> sortedBookmarks = new ArrayList<>(bookmarks);
            Collections.sort(sortedBookmarks, new Comparator<QuestionModel>() {
                @Override
                public int compare(QuestionModel q1, QuestionModel q2) {
                    String testId1 = q1.getTestId() != null ? q1.getTestId() : "";
                    String testId2 = q2.getTestId() != null ? q2.getTestId() : "";
                    return testId1.compareTo(testId2);
                }
            });
            bookmarkList.postValue(sortedBookmarks);
        }
    }

    public void rebookmarkQuestion(QuestionModel question) {
        authRepository.rebookmarkByQuestionId(question);
        // Update with sorted list
        List<QuestionModel> bookmarks = authRepository.getBookmarkList();
        List<QuestionModel> sortedBookmarks = new ArrayList<>(bookmarks);
        Collections.sort(sortedBookmarks, new Comparator<QuestionModel>() {
            @Override
            public int compare(QuestionModel q1, QuestionModel q2) {
                String testId1 = q1.getTestId() != null ? q1.getTestId() : "";
                String testId2 = q2.getTestId() != null ? q2.getTestId() : "";
                return testId1.compareTo(testId2);
            }
        });
        bookmarkList.postValue(sortedBookmarks);
    }
}