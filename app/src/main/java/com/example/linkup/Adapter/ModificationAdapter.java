package com.example.linkup.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.EventOperation.EventActivity;
import com.example.linkup.Object.Events;
import com.example.linkup.Object.Modifications;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ModificationAdapter extends RecyclerView.Adapter<ModificationAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Modifications> modificationsArrayList = new ArrayList<>();
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db

    // Constructor
    public ModificationAdapter(Context context,ArrayList<Modifications> modificationsArrayList ) {
        this.context = context;
        this.modificationsArrayList = modificationsArrayList;

        // [START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END configuration]
    }
    @NonNull
    @Override
    public ModificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.modification_item, parent, false);

        return new ModificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModificationAdapter.ViewHolder holder, int position) {
        final Modifications modification = modificationsArrayList.get(position);

        // [START config_layout]
        holder.content.setText(modification.getContent());
        // [END config_layout]
    }
    @Override
    public int getItemCount() {
        return modificationsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
        }
    }

}

