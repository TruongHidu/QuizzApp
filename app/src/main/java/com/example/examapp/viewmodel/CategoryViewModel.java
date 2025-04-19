package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.model.CategoryModel;
import com.example.examapp.repository.AuthRepository;

import java.util.List;

public class CategoryViewModel extends ViewModel {
    private AuthRepository authRepository;

    public CategoryViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<List<CategoryModel>> getCategories() {
        return authRepository.getCategories();
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }

    public void loadCategories() {
        authRepository.loadCategories();
    }
}