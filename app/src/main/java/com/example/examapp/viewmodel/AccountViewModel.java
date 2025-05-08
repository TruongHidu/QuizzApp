package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.model.RankModel;
import com.example.examapp.repository.AuthRepository;

public class AccountViewModel extends ViewModel {
    private AuthRepository authRepository;

    public AccountViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<RankModel> getUserPerformance() {
        return authRepository.getUserPerformance();
    }

    public LiveData<Integer> getUserCount() {
        return authRepository.getUserCount();
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }

    public void loadAccountData() {
        authRepository.loadTopUsers();
        authRepository.loadUserCount();
    }

    public void clearData() {
        authRepository.clearData();
    }
}