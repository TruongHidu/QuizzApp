package com.example.examapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.model.RankModel;

import java.util.ArrayList;
import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder> {
    private List<RankModel> rankList;

    public RankAdapter(List<RankModel> rankList) {
        this.rankList = new ArrayList<>(rankList);
    }

    public void updateData(List<RankModel> newRankList) {
        this.rankList.clear();
        this.rankList.addAll(newRankList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RankModel rank = rankList.get(position);
        holder.setData(rank.getName(), rank.getScore(), rank.getRank());
    }

    @Override
    public int getItemCount() {
        return rankList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName, txtTotalScore, txtRank, txtImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtTotalScore = itemView.findViewById(R.id.txtTotalScore);
            txtRank = itemView.findViewById(R.id.txtRank);
            txtImage = itemView.findViewById(R.id.txtImage);
        }

        void setData(String name, int totalScore, int rank) {
            txtName.setText(name);
            txtTotalScore.setText("Score: " + totalScore);
            txtRank.setText("Rank - " + rank);
            txtImage.setText(name != null && !name.isEmpty() ? name.toUpperCase().substring(0, 1) : "N");
        }
    }
}