package com.example.examapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.examapp.database.DbQuery;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.ProfileModel;
import com.example.examapp.repository.AuthRepository;

public class MainViewModel extends ViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<ProfileModel> profileData = new MutableLiveData<>();
    private MutableLiveData<Boolean> dataLoaded = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

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

    public void loadData() {
        authRepository.loadData(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                dataLoaded.postValue(true);
                profileData.postValue(new ProfileModel(
                        DbQuery.myProfile.getName(),
                        DbQuery.myProfile.getEmail(),
                        DbQuery.myProfile.getPhone(),
                        DbQuery.myProfile.getBookmarkCount()
                ));
            }

            @Override
            public void onFailture() {
                dataLoaded.postValue(false);
                errorMessage.postValue("Failed to load user data");
            }
        });
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }
}