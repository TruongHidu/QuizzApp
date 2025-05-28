package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.repository.AuthRepository;

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
                        bookmarkList.postValue(authRepository.getBookmarkList());
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
            bookmarkList.postValue(authRepository.getBookmarkList());
        }
    }

    public void rebookmarkQuestion(QuestionModel question) {
        authRepository.rebookmarkByQuestionId(question);
        bookmarkList.postValue(authRepository.getBookmarkList());
    }
}