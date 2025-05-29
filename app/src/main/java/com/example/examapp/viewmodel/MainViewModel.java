package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.database.DbQuery;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.CategoryModel;
import com.example.examapp.model.ProfileModel;
import com.example.examapp.repository.AuthRepository; // Assuming AuthRepository handles category loading now
import java.util.List;

public class MainViewModel extends ViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<ProfileModel> profileData = new MutableLiveData<>();
    private MutableLiveData<Boolean> dataLoaded = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<List<CategoryModel>> categories = new MutableLiveData<>(); // Add for categories

    public MainViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<ProfileModel> getProfileData() {
        return profileData;
    }

    public LiveData<Boolean> getDataLoaded() {
        return dataLoaded;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<CategoryModel>> getCategories() { // Getter for categories
        return categories;
    }

    public void loadAllInitialData() { // New method to load all initial data
        authRepository.loadUserDataAndCategories(new MyCompleteListener() { // Assuming this new method exists in AuthRepository
            @Override
            public void onSuccess() {
                // User profile data
                profileData.postValue(new ProfileModel(
                        DbQuery.myProfile.getName(),
                        DbQuery.myProfile.getEmail(),
                        DbQuery.myProfile.getPhone(),
                        DbQuery.myProfile.getBookmarkCount()
                ));

                // Categories
                categories.postValue(DbQuery.g_categoryList); // Assuming DbQuery holds the loaded categories

                dataLoaded.postValue(true);
            }

            @Override
            public void onFailture() {
                dataLoaded.postValue(false);
                errorMessage.postValue("Failed to load initial data");
            }
        });
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }
}