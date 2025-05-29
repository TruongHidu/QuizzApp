package com.example.examapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.examapp.R;
import com.example.examapp.databinding.FragmentAccountBinding;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.AccountViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private AccountViewModel viewModel;
    private ProgressDialogUtil progressDialogUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("My Account");

        // Initialize progress dialog
        progressDialogUtil = new ProgressDialogUtil(getContext());
        progressDialogUtil.show("Loading ...");

        // Observe ViewModel data
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            updateProfileUI(profile != null ? profile.getName() : null);
        });

        viewModel.getUserPerformance().observe(getViewLifecycleOwner(), performance -> {
            progressDialogUtil.dismiss();
            if (performance != null && performance.getRank() > 0) {
                binding.txtOverRollScore.setText(String.valueOf(performance.getScore()));
                binding.txtRank.setText(String.valueOf(performance.getRank()));
            } else {
                binding.txtOverRollScore.setText("0");
                binding.txtRank.setText("NA");
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            progressDialogUtil.dismiss();
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load data
        viewModel.loadAccountData();

        // Set up click listeners
        binding.btnBookMarkQuestion.setOnClickListener(v -> {
            progressDialogUtil.dismiss();
            startActivity(new Intent(getContext(), BookMarkActivity.class));
        });

        binding.btnLeaderboard.setOnClickListener(v -> {
            progressDialogUtil.dismiss();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LeaderBoardFragment())
                    .addToBackStack(null)
                    .commit();
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav_bar);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_leaderboard);
            }
        });

        binding.btnProfile.setOnClickListener(v -> {
            progressDialogUtil.dismiss();
            startActivity(new Intent(getContext(), MyProfileActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> {
            progressDialogUtil.dismiss();
            viewModel.clearData();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadAccountData(); // Reload profile data to ensure UI is up-to-date
    }

    private void updateProfileUI(String name) {
        binding.txtName.setText(name != null && !name.isEmpty() ? name : "");
        binding.imgName.setText(name != null && !name.isEmpty() ? name.toUpperCase().substring(0, 1) : "N");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressDialogUtil.dismiss();
        binding = null;
    }
}