package com.example.examapp;

import static com.example.examapp.database.DbQuery.g_userCount;
import static com.example.examapp.database.DbQuery.g_userList;
import static com.example.examapp.database.DbQuery.myPerformanece;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.examapp.activities.ScoreActivity;
import com.example.examapp.adapter.RankAdapter;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.FragmentLeaderBoardBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;


public class LeaderBoardFragment extends Fragment {
    FragmentLeaderBoardBinding binding;
    private RankAdapter adapter;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderBoardBinding.inflate(inflater, container, false);

        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Leaderboard");


        progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Loading ...");
        progressDialog.show();



        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rcvRank.setLayoutManager(layoutManager);

        adapter = new RankAdapter(g_userList);
        binding.rcvRank.setAdapter(adapter);

        DbQuery.getTopUsers(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                adapter.notifyDataSetChanged();
                if(DbQuery.myPerformanece.getScore() != 0){

                    if(DbQuery.isMeOnTopList){
                        caculateRank();
                    }
                    binding.txtTotalScore.setText("Score: " + DbQuery.myPerformanece.getScore());
                    binding.txtRank.setText("Rank - " + DbQuery.myPerformanece.getRank());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailture() {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        });

        binding.txtTotalUsers.setText("Total users: " + DbQuery.g_userCount);



        return binding.getRoot();
    }

    private void caculateRank() {
        if (g_userList.isEmpty()) {
            myPerformanece.setRank(1); // Nếu danh sách rỗng, đặt rank là 1
            return;
        }

        int rank = 1; // Rank bắt đầu từ 1
        for (int i = 0; i < g_userList.size(); i++) {
            if (myPerformanece.getScore() < g_userList.get(i).getScore()) {
                rank++; // Nếu có người điểm cao hơn, tăng rank
            }
        }

        myPerformanece.setRank(rank);
    }


}