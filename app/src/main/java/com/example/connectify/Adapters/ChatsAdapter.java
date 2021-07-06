package com.example.connectify.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.Models.Chats;
import com.example.connectify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
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
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {

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

        holder.message_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showing delete message confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");

                //delete button
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });

                //cancel button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });

                //show dialog
                builder.create().show();
            }
        });

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

    private void deleteMessage(int position) {

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //getting timestamp of clicked message
        String mTimeStamp = chatsList.get(position).getTimeStamp();

        //comparing timestamp of clicked message with timestamps all messages in chat
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = reference.orderByChild("timeStamp").equalTo(mTimeStamp);

        //when value of timestamp matches
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    if (dataSnapshot.child("sender").getValue().equals(myUid)) {

                        //removing the message from chats
                        //dataSnapshot.getRef().removeValue();

                        //setting the value of message as "This message was deleted"
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        dataSnapshot.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "Message deleted!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, "You can delete your message only", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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
        LinearLayout message_layout;

        public MyHolder (@NonNull View view) {

            super(view);

            //init views
            imagepr = view.findViewById(R.id.imagepr);
            messagepr = view.findViewById(R.id.messagepr);
            timepr = view.findViewById(R.id.timepr);
            isSeen = view.findViewById(R.id.isSeen);
            message_layout = view.findViewById(R.id.message_layout);
        }

    }
}
