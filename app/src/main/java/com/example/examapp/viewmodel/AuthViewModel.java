package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.ProfileModel;
import com.example.examapp.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;
    private LiveData<String> errorMessage;
    private LiveData<ProfileModel> userProfile;

    public AuthViewModel() {
        authRepository = new AuthRepository();
        errorMessage = authRepository.getErrorMessage();
        userProfile = authRepository.getUserProfile();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<ProfileModel> getUserProfile() {
        return userProfile;
    }

    public void signUp(String email, String password, String fullName, MyCompleteListener listener) {
        authRepository.signUp(email, password, fullName, listener);
    }

    public void saveUserData(String name, MyCompleteListener listener) {
        authRepository.saveUserData(name, listener);
    }

    public void loadUserProfile() {
        authRepository.getUserData(new MyCompleteListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailture() {
            }
        });
    }
}