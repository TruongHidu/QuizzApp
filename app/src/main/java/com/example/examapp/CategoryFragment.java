package com.example.examapp;

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
import com.example.examapp.databinding.FragmentCategoryBinding;
import com.example.examapp.model.CategoryModel;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.CategoryViewModel;

import java.util.ArrayList;

public class CategoryFragment extends Fragment {
    private FragmentCategoryBinding binding;
    private CategoryViewModel viewModel;
    private CategoryAdapter adapter;
    private ProgressDialogUtil progressDialogUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Category");

        progressDialogUtil = new ProgressDialogUtil(getContext());
        progressDialogUtil.show("Loading ...");

        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new CategoryAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            progressDialogUtil.dismiss();
            if (categories != null) {
                adapter.updateData(categories);
                binding.recyclerView.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);
                binding.emptyView.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            progressDialogUtil.dismiss();
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadCategories();

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