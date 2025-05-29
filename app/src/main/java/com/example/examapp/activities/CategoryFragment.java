package com.example.examapp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.examapp.adapter.CategoryAdapter;
import com.example.examapp.database.DbQuery; // Assuming DbQuery.g_catList is updated after load
import com.example.examapp.databinding.FragmentCategoryBinding;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.MainViewModel; // Use MainViewModel here

import java.util.ArrayList;

public class CategoryFragment extends Fragment {
    private FragmentCategoryBinding binding;
    private MainViewModel mainViewModel; // Use MainViewModel
    private CategoryAdapter adapter;
    private ProgressDialogUtil progressDialogUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class); // Get from Activity scope
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Category");

        progressDialogUtil = new ProgressDialogUtil(getContext());
        // Show progress dialog only if categories are not yet loaded
        if (mainViewModel.getCategories().getValue() == null || mainViewModel.getCategories().getValue().isEmpty()) {
            progressDialogUtil.show("Loading...");
        }


        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new CategoryAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        // Observe categories from MainViewModel
        mainViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            progressDialogUtil.dismiss();
            if (categories != null) {
                adapter.updateData(categories);
                binding.recyclerView.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);
                binding.emptyView.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        // Observe errors from MainViewModel (if it has error propagation for category loading)
        mainViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            progressDialogUtil.dismiss();
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // No need to call viewModel.loadCategories() here anymore.
        // MainActivity handles the initial data load for all components.

        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        progressDialogUtil.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressDialogUtil.dismiss();
        binding = null;
        adapter = null;
    }
}