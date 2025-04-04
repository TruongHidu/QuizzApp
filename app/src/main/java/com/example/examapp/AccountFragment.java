package com.example.examapp;

import static com.example.examapp.database.DbQuery.g_userCount;
import static com.example.examapp.database.DbQuery.g_userList;
import static com.example.examapp.database.DbQuery.myPerformanece;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.examapp.activities.LoginActivity;
import com.example.examapp.activities.MyProfileActivity;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.FragmentAccountBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.google.firebase.auth.FirebaseAuth;


public class AccountFragment extends Fragment {


    private FragmentAccountBinding binding;
    private Dialog progressDialog;
    private TextView dialogText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("My Account");



        progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Loading ...");


        binding.txtName.setText(DbQuery.myProfile.getName());
        binding.txtOverRollScore.setText(String.valueOf(DbQuery.myPerformanece.getScore()));
        binding.imgName.setText(DbQuery.myProfile.getName().toUpperCase().substring(0, 1));


      if(DbQuery.g_userList.size() == 0){
          progressDialog.show();
          DbQuery.getTopUsers(new MyCompleteListener() {
              @Override
              public void onSuccess() {
                  if(DbQuery.myPerformanece.getScore() != 0){

                      if(DbQuery.isMeOnTopList){
                          caculateRank();
                      }
                      binding.txtOverRollScore.setText(String.valueOf(DbQuery.myPerformanece.getScore()));
                      binding.txtRank.setText(String.valueOf(DbQuery.myPerformanece.getRank()));


                  }
                  progressDialog.dismiss();
              }

              @Override
              public void onFailture() {
                  progressDialog.dismiss();
                  Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();

              }
          });
      }else {
          binding.txtOverRollScore.setText(String.valueOf(DbQuery.myPerformanece.getScore()));
          if(myPerformanece.getScore() != 0)
              binding.txtRank.setText(String.valueOf(DbQuery.myPerformanece.getRank()));

      }


        binding.btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        binding.btnProfile.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), MyProfileActivity.class);
            startActivity(intent);

        });

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