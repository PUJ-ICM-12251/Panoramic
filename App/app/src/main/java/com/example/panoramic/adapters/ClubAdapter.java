package com.example.panoramic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.panoramic.R;
import com.example.panoramic.databinding.ClubAdapterBinding;
import com.example.panoramic.model.NightClub;

import java.util.ArrayList;

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ViewHolder> {

    ArrayList<NightClub> nightClubs;

    ClubAdapterBinding binding;

    public ClubAdapter(ArrayList<NightClub> nightClubs){
        this.nightClubs=nightClubs;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate= LayoutInflater.from(parent.getContext()).inflate(R.layout.club_adapter,parent,false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.clubName.setText(nightClubs.get(position).getName());
        holder.clubAddress.setText(nightClubs.get(position).getAddress());
//        int drawableResourceId = holder.itemView.getContext().getResources().getIdentifier(nightClubs.get(position).getImage(),"drawable",holder.itemView.getContext().getPackageName());
        Glide.with(holder.itemView.getContext()).load(nightClubs.get(position).getImage()).into(holder.clubImage);
    }

    @Override
    public int getItemCount() {
        return nightClubs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView clubName;
        TextView clubAddress;
        ImageView clubImage;
        ConstraintLayout mainLayout;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            clubName=itemView.findViewById((R.id.BarName));
            clubAddress=itemView.findViewById(R.id.BarSecondName);
            clubImage= itemView.findViewById(R.id.barimage);
            mainLayout=itemView.findViewById(R.id.mainLayout);
        }
    }
}
