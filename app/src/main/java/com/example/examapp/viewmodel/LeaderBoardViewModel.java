package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.model.RankModel;
import com.example.examapp.repository.AuthRepository;

import java.util.List;

public class LeaderBoardViewModel extends ViewModel {
    private AuthRepository authRepository;

    public LeaderBoardViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<List<RankModel>> getTopUsers() {
        return authRepository.getTopUsers();
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

    public void loadLeaderboardData() {
        authRepository.loadTopUsers();
        authRepository.loadUserCount();
    }
}