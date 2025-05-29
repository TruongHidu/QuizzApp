package com.example.examapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.examapp.R;
import com.example.examapp.databinding.ActivityMainBinding;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.MainViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private ActionBarDrawerToggle toggle;
    private TextView drawerUserNameText, drawerUserEmailText, imgName;
    // private ProgressDialogUtil progressDialogUtil; // No longer needed here if fragments manage their own loading indicators

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.navView.bringToFront();
        imgName = binding.navView.getHeaderView(0).findViewById(R.id.imgName);
        drawerUserNameText = binding.navView.getHeaderView(0).findViewById(R.id.txtNameDrawer);
        drawerUserEmailText = binding.navView.getHeaderView(0).findViewById(R.id.txtEmailDrawer);

        setSupportActionBar(binding.toolbar);
        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        // Observe profile data (unchanged)
        viewModel.getProfileData().observe(this, profile -> {
            if (profile != null) {
                drawerUserNameText.setText(profile.getName().toUpperCase());
                drawerUserEmailText.setText(profile.getEmail());
                imgName.setText(profile.getName().toUpperCase().substring(0, 1));
            }
        });

        // Observe errors (unchanged)
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        // Observe data loading status
        // Instead of navigateToFragment here, we just know data is loaded.
        // The fragment will observe the categories directly from the MainViewModel.
        viewModel.getDataLoaded().observe(this, loaded -> {
            if (loaded != null && loaded) {
                // Initial data (profile and categories) is loaded.
                // We can now safely set the initial fragment.
                // Call navigateToFragment only once after initial data is loaded
                navigateToFragment();
            }
        });

        // Check login status and load data
        if (viewModel.isUserLoggedIn()) {
            viewModel.loadAllInitialData(); // Call the new method to load all initial data
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Bottom Navigation (unchanged)
        binding.bottomNavBar.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new CategoryFragment();
            } else if (itemId == R.id.nav_leaderboard) {
                selectedFragment = new LeaderBoardFragment();
            } else if (itemId == R.id.nav_account) {
                selectedFragment = new AccountFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Navigation Drawer Item Clicks (unchanged, but ensure fragments are created to observe)
        binding.navView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                selectedFragment = new AccountFragment();
                binding.bottomNavBar.setSelectedItemId(R.id.nav_account);
            } else if (id == R.id.nav_home) {
                selectedFragment = new CategoryFragment();
                binding.bottomNavBar.setSelectedItemId(R.id.nav_home);
            } else if (id == R.id.nav_leaderboard) {
                selectedFragment = new LeaderBoardFragment();
                binding.bottomNavBar.setSelectedItemId(R.id.nav_leaderboard);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void navigateToFragment() {
        String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");
        Fragment selectedFragment;
        int navItemId;

        if (fragmentToLoad != null) {
            switch (fragmentToLoad) {
                case "LEADERBOARD":
                    selectedFragment = new LeaderBoardFragment();
                    navItemId = R.id.nav_leaderboard;
                    break;
                case "ACCOUNT":
                    selectedFragment = new AccountFragment();
                    navItemId = R.id.nav_account;
                    break;
                default:
                    selectedFragment = new CategoryFragment();
                    navItemId = R.id.nav_home;
                    break;
            }
        } else {
            selectedFragment = new CategoryFragment();
            navItemId = R.id.nav_home;
        }

        binding.bottomNavBar.setSelectedItemId(navItemId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}