package com.example.examapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.model.RankModel;

import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder>{
    private List<RankModel> list;
    public RankAdapter(List<RankModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RankAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rank_item_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankAdapter.ViewHolder viewHolder, int i) {
        String name = list.get(i).getName();
        int totalScore = list.get(i).getScore();
        int rank = list.get(i).getRank();
        viewHolder.setData(name, totalScore, rank);

    }

    @Override
    public int getItemCount() {
        if (list.size() > 10 ){
            return 10;
        }else{
            return list.size();
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView txtName, txtTotalScore, txtRank, txtImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtTotalScore = itemView.findViewById(R.id.txtTotalScore);
            txtRank = itemView.findViewById(R.id.txtRank);
            txtImage = itemView.findViewById(R.id.txtImage);


        }
        private void setData(String name, int totalScore, int rank){
            txtName.setText(name);
            txtTotalScore.setText("Score: " + totalScore);
            txtRank.setText("Rank - " + rank);
            txtImage.setText(name.toUpperCase().substring(0,1));

        }
    }

}
