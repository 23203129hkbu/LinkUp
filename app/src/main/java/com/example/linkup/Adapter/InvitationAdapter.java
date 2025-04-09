package com.example.linkup.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Object.Events;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Users> usersArrayList;
    Events event;

    // Constructor
    public InvitationAdapter(Context context, ArrayList<Users> usersArrayList, Events event) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        this.event = event;
    }


    @NonNull
    @Override
    public InvitationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.invitation_item, parent, false);

        return new InvitationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationAdapter.ViewHolder holder, int position) {
        final Users user = usersArrayList.get(position);
        // [START config_layout]
        Picasso.get().load(user.getAvatarURL()).into(holder.avatar);
        holder.username.setText(user.getUsername());
        // [END config_layout]
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView username, btnInvite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            btnInvite = itemView.findViewById(R.id.btnInvite);
        }
    }
}


