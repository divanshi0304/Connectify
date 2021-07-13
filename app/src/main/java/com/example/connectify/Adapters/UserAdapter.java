package com.example.connectify.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.BuildConfig;
import com.example.connectify.ChatActivity;
import com.example.connectify.Models.Users;
import com.example.connectify.R;
import com.example.connectify.UserProfileActivity;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    //user list to be displayed
    Context context;
    List<Users> usersList;

    //constructor
    public UserAdapter(Context context, List<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout (users row)
        View view = LayoutInflater.from(context).inflate(R.layout.users_row, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //getting data of different users
        String userName = usersList.get(position).getName();
        String userImage = usersList.get(position).getImage();
        String userEmail = usersList.get(position).getEmail();
        String userUid = usersList.get(position).getUid();

        //setting data of different users
        holder.mnamepr.setText(userName);
        holder.memailpr.setText(userEmail);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_user).into(holder.mavatarpr);
        }
        catch (Exception exception) {

        }

        //handling item on clicking
        holder.itemView.setOnClickListener(v -> {

            /*Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userUid", userUid);
            context.startActivity(intent);
*/
            //showing dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            String[] options = {"Posts", "Chats"};

            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //handling dialogs on clicking
                    if (which == 0) {
                        //profile is clicked, go to user's posts and data using his uid
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("uid", userUid);
                        context.startActivity(intent);
                    }
                    else if (which == 1) {
                        //chat is clicked
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("userUid", userUid);
                        context.startActivity(intent);
                    }
                }
            });

            //create and show dialog
            builder.create().show();
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //views holder
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView mavatarpr;
        TextView mnamepr, memailpr;

        public MyHolder (@NonNull View itemView) {

            super(itemView);

            //init views
            mavatarpr = itemView.findViewById(R.id.avatarpr);
            mnamepr = itemView.findViewById(R.id.namepr);
            memailpr = itemView.findViewById(R.id.emailpr);
        }
    }

}
