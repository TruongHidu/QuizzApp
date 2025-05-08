package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;
    private LiveData<String> errorMessage;

    public AuthViewModel() {
        authRepository = new AuthRepository();
        errorMessage = authRepository.getErrorMessage();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void signUp(String email, String password, String fullName, MyCompleteListener listener) {
        authRepository.signUp(email, password, fullName, listener);
    }

    public void saveUserData(String name, MyCompleteListener listener) {
        authRepository.saveUserData(name, listener);
    }
//    public void checkPhone(String phone, MyCompleteListener listener){
//        authRepository.checkPhone(phone, listener);
//    }


}