package com.example.connectify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.Models.Comments;
import com.example.connectify.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {

    Context context;
    List<Comments> commentsList;

    public CommentsAdapter(Context context, List<Comments> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding the comments row xml file
        View view = LayoutInflater.from(context).inflate(R.layout.comments_row, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.MyHolder holder, int position) {
        //getting the data
        String uid = commentsList.get(position).getUid();
        String name = commentsList.get(position).getuName();
        String email = commentsList.get(position).getuEmail();
        String image = commentsList.get(position).getuImage();
        String cid = commentsList.get(position).getcId();
        String comment = commentsList.get(position).getComment();
        String timeStamp = commentsList.get(position).getTimeStamp();

        //converting timestamp to desired time format
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(timeStamp));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String pTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //setting data
        holder.namepr.setText(name);
        holder.commentpr.setText(comment);
        holder.timepr.setText(pTime);

        //setting user image
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_user).into(holder.imagepr);
        }
        catch (Exception e) {

        }

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        //declaring views from comments row xml file
        ImageView imagepr;
        TextView namepr, commentpr, timepr;

        public MyHolder (@NonNull View view) {
            super(view);
            imagepr = view.findViewById(R.id.imagepr);
            namepr = view.findViewById(R.id.namepr);
            commentpr = view.findViewById(R.id.commentpr);
            timepr = view.findViewById(R.id.timepr);
        }
    }
}
