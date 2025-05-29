package com.example.examapp.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.examapp.R;
import com.example.examapp.adapter.RankAdapter;
import com.example.examapp.databinding.FragmentLeaderBoardBinding;
import com.example.examapp.viewmodel.LeaderBoardViewModel;

import java.util.ArrayList;

public class LeaderBoardFragment extends Fragment {
    private FragmentLeaderBoardBinding binding;
    private LeaderBoardViewModel viewModel;
    private Dialog progressDialog;
    private TextView dialogText;
    private RankAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLeaderBoardBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(LeaderBoardViewModel.class);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Leaderboard");

        // Initialize progress dialog
        progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Loading ...");
        progressDialog.show();

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rcvRank.setLayoutManager(layoutManager);
        adapter = new RankAdapter(new ArrayList<>());
        binding.rcvRank.setAdapter(adapter);

        // Observe ViewModel data
        viewModel.getTopUsers().observe(getViewLifecycleOwner(), topUsers -> {
            progressDialog.dismiss();
            if (topUsers != null) {
                adapter.updateData(topUsers); // Update existing adapter
                binding.rcvRank.setVisibility(topUsers.isEmpty() ? View.GONE : View.VISIBLE);
                binding.emptyView.setVisibility(topUsers.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getUserPerformance().observe(getViewLifecycleOwner(), performance -> {
            progressDialog.dismiss();
            if (performance != null && performance.getScore() != 0) {

                binding.txtTotalScore.setText("Score: " + performance.getScore());
                binding.txtRank.setText("Rank - " + (performance.getRank() > 0 ? performance.getRank() : "NA"));
            }
        });

        viewModel.getUserCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.txtTotalUsers.setText("Total users: " + count);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            progressDialog.dismiss();
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load data
        viewModel.loadLeaderboardData();

        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        progressDialog.dismiss(); // Dismiss dialog when fragment is stopped
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }
}