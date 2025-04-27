package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.database.DbQuery;
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

    public void loadBookMarks(MyCompleteListener listener) {
        authRepository.loadBmIds(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                authRepository.loadBookMarks(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        bookmarkList.postValue(DbQuery.g_bookmarkList);
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

    public void updateBookmark(int questionIndex, boolean isBookmarked) {
        authRepository.updateBookmark(questionIndex, isBookmarked);
        bookmarkList.postValue(DbQuery.g_bookmarkList); // Update LiveData
    }
}