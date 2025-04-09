package com.example.examapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.examapp.AccountFragment;
import com.example.examapp.CategoryFragment;
import com.example.examapp.LeaderBoardFragment;
import com.example.examapp.R;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private TextView drawerUserNameText, drawerUserEmailText, imgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // Setup DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        imgName = navigationView.getHeaderView(0).findViewById(R.id.imgName);
        drawerUserNameText = navigationView.getHeaderView(0).findViewById(R.id.txtNameDrawer);
        drawerUserEmailText = navigationView.getHeaderView(0).findViewById(R.id.txtEmailDrawer);

        // Cập nhật thông tin user lên header
        drawerUserNameText.setText(DbQuery.myProfile.getName().toUpperCase());
        drawerUserEmailText.setText(DbQuery.myProfile.getEmail());
        imgName.setText(DbQuery.myProfile.getName().toUpperCase().substring(0, 1));


        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();




// Xử lý Intent để mở đúng Fragment
        String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");

        if (fragmentToLoad != null) {
            Fragment selectedFragment = null;

            if (fragmentToLoad.equals("LEADERBOARD")) {
                selectedFragment = new LeaderBoardFragment();
                binding.bottomNavBar.setSelectedItemId(R.id.nav_leaderboard);
            } else if (fragmentToLoad.equals("ACCOUNT")) {
                selectedFragment = new AccountFragment();
                binding.bottomNavBar.setSelectedItemId(R.id.nav_account);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
        } else {
            // Chỉ load mặc định nếu không có yêu cầu cụ thể
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CategoryFragment())
                    .commit();
        }


        // Bottom Navigation
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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });

        // Handle Navigation Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                selectedFragment = new AccountFragment();
            } else if (id == R.id.nav_home) {
                selectedFragment = new CategoryFragment();
            } else if (id == R.id.nav_leaderboard) {
                selectedFragment = new LeaderBoardFragment();
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
