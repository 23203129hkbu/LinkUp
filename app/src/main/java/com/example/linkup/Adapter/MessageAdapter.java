package com.example.linkup.Adapter;

import static com.example.linkup.ChatOperation.ChatRoomActivity.receiverImg;
import static com.example.linkup.ChatOperation.ChatRoomActivity.senderImg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Object.Messages;
import com.example.linkup.Object.Posts;
import com.example.linkup.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Messages> messagesArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    //
    int ITEM_SEND = 1;
    int ITEM_RECEIVE = 2;


    // Constructor
    public MessageAdapter(Context context, ArrayList<Messages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new senderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_layout, parent, false);
            return new reciverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Messages message = messagesArrayList.get(position);
        // [START config_layout]
        if (holder.getClass() == senderViewHolder.class) {
            senderViewHolder viewHolder = (senderViewHolder) holder;
            viewHolder.msgTxt.setText(message.getContent());
            Picasso.get().load(senderImg).into(viewHolder.circleImageView);
        } else {
            reciverViewHolder viewHolder = (reciverViewHolder) holder;
            viewHolder.msgTxt.setText(message.getContent());
            Picasso.get().load(receiverImg).into(viewHolder.circleImageView);


        }
        // [END config_layout]
    }
    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }
    @Override
    public int getItemViewType(int position) {
        Messages messages = messagesArrayList.get(position);
        if (auth.getUid().equals(messages.getUID())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECEIVE;
        }
    }

    class senderViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView msgTxt;

        public senderViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.senderAvatar);
            msgTxt = itemView.findViewById(R.id.senderTxt);

        }
    }

    class reciverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView msgTxt;

        public reciverViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.receiverAvatar);
            msgTxt = itemView.findViewById(R.id.receiverTxt);
        }
    }


}