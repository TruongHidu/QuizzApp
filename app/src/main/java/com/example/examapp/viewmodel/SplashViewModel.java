package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.repository.AuthRepository;
import com.example.examapp.handlerlistener.MyCompleteListener;

public class SplashViewModel extends ViewModel {
    private AuthRepository authRepository;

    public SplashViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Boolean> getLoginStatus() {
        return authRepository.getLoginStatus();
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    public void loadData(MyCompleteListener listener) {
        authRepository.loadData(listener);
    }
}