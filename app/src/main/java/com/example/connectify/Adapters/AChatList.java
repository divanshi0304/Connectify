package com.example.connectify.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.ChatActivity;
import com.example.connectify.Models.Users;
import com.example.connectify.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class AChatList extends RecyclerView.Adapter<AChatList.MyHolder> {

    Context context;
    List<Users> usersList;
    private HashMap<String, Object> lastMessageMap;

    //constructor
    public AChatList(Context context, List<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        //inflating layout of chat list row xml file
        View view = LayoutInflater.from(context).inflate(R.layout.chatlist_row, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull AChatList.MyHolder holder, int position) {

        //getting data
        String userUid = usersList.get(position).getUid();
        String userImage = usersList.get(position).getImage();
        String userName = usersList.get(position).getName();
        String lastMessage = (String) lastMessageMap.get(userUid);

        //setting data
        holder.namepr.setText(userName);
        if (lastMessage==null || lastMessage.equals("default")) {
            holder.last_messagepr.setVisibility(View.GONE);
        }
        else {
            holder.last_messagepr.setVisibility(View.VISIBLE);
            holder.last_messagepr.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_user).into(holder.imagepr);
        }
        catch (Exception exception) {
            //Picasso.get().load(R.drawable.ic_default_user).into(holder.imagepr);
        }

        //setting online status of user
        if (usersList.get(position).getOnlineStatus().equals("online")) {
            //if online
            holder.onlineStatuspr.setImageResource(R.drawable.online_circle);
        }
        else {
            //if offline
            holder.onlineStatuspr.setImageResource(R.drawable.offline_circle);
        }

        //handling clicking on user in chat list
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //starting chat activity with user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userUid", userUid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        //return size of the list
        return usersList.size();
    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //views from chatlist row xml file
        ImageView imagepr, onlineStatuspr;
        TextView namepr, last_messagepr;

        public MyHolder (@NonNull View view) {
            super(view);

            //init views
            imagepr = view.findViewById(R.id.imagepr);
            onlineStatuspr = view.findViewById(R.id.onlineStatuspr);
            namepr = view.findViewById(R.id.namepr);
            last_messagepr = view.findViewById(R.id.last_messagepr);
        }
    }
}
