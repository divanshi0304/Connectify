package com.example.connectify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.Models.Chats;
import com.example.connectify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    Context context;
    List<Chats> chatsList;
    String imageUri;

    FirebaseUser firebaseUser;

    public ChatsAdapter(Context context, List<Chats> chatsList, String imageUri) {
        this.context = context;
        this.chatsList = chatsList;
        this.imageUri = imageUri;
    }

    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflating layouts for receiver and sender
        if (viewType==MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_row_right, parent, false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_row_left, parent, false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //getting data
        String message = chatsList.get(position).getMessage();
        String timeStamp = chatsList.get(position).getTimeStamp();

        //converting time stamp to desired format
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        try {
            calendar.setTimeInMillis(Long.parseLong(timeStamp));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String dateTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //setting data
        holder.messagepr.setText(message);
        holder.timepr.setText(dateTime);
        try {
            Picasso.get().load(imageUri).into(holder.imagepr);
        }
        catch (Exception exception) {

        }

        //setting status of message (seen/delivered)
        if (position==chatsList.size()-1) {
            if (chatsList.get(position).isSeen()) {
                holder.isSeen.setText("Seen");
            }
            else {
                holder.isSeen.setText("Delivered");
            }
        }
        else {
            holder.isSeen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //getting currently signed in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (chatsList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views
        ImageView imagepr;
        TextView messagepr, timepr, isSeen;

        public MyHolder (@NonNull View view) {

            super(view);

            //init views
            imagepr = view.findViewById(R.id.imagepr);
            messagepr = view.findViewById(R.id.messagepr);
            timepr = view.findViewById(R.id.timepr);
            isSeen = view.findViewById(R.id.isSeen);
        }

    }
}
