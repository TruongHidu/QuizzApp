package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.repository.AuthRepository;
import com.example.examapp.handlerlistener.MyCompleteListener;

public class LoginViewModel extends ViewModel {
    private AuthRepository authRepository;

    public LoginViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Boolean> getLoginStatus() {
        return authRepository.getLoginStatus();
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }

    public void login(String email, String password, MyCompleteListener listener) {
        authRepository.login(email, password, listener);
    }

    public boolean validateData(String email, String password) {
        if (email.isEmpty()) {
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }
        return !password.isEmpty();
    }
}