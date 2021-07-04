package com.example.connectify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.Models.Posts;
import com.example.connectify.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    Context context;
    List<Posts> postsList;

    //constructor


    public PostsAdapter(Context context, List<Posts> postsList) {
        this.context = context;
        this.postsList = postsList;
    }

    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        //inflating posts row layout
        View view = LayoutInflater.from(context).inflate(R.layout.posts_row, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PostsAdapter.MyHolder holder, int position) {

        //getting data
        String uid = postsList.get(position).getUid();
        String uName = postsList.get(position).getuName();
        String uEmail = postsList.get(position).getuEmail();
        String uImage = postsList.get(position).getuImage();
        String pId = postsList.get(position).getpId();
        String pTitle = postsList.get(position).getpTitle();
        String pDescription = postsList.get(position).getpDescription();
        String pImage = postsList.get(position).getpImage();
        String pTimeStamp = postsList.get(position).getpTime();

        //converting timestamp to desired time format
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String pTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //setting other data
        holder.u_name.setText(uName);
        holder.p_time.setText(pTime);
        holder.p_title.setText(pTitle);
        holder.p_description.setText(pDescription);

        //setting user's profile picture
        try {
            Picasso.get().load(uImage).placeholder(R.drawable.ic_default_user).into(holder.u_image);
        }
        catch (Exception exception) {

        }

        //setting post image; if no image, hide image view
        if (pImage.equals("noImage"))
        {
            //hiding image view
            holder.p_image.setVisibility(View.GONE);
        }
        else {
            try {
                Picasso.get().load(pImage).into(holder.p_image);
            } catch (Exception exception) {

            }
        }

        //handling button clicks
        holder.more_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
            }
        });

        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context, "Like", Toast.LENGTH_SHORT).show();
            }
        });

        holder.comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show();
            }
        });

        holder.share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views from xml file
        ImageView u_image, p_image;
        TextView u_name, p_title, p_description, p_time;
        Button like_btn, comment_btn, share_btn;
        ImageButton more_btn;

        public MyHolder(@NonNull View view) {

            super(view);

            //init views
            u_image = view.findViewById(R.id.u_image);
            p_image = view.findViewById(R.id.p_image);
            u_name = view.findViewById(R.id.u_name);
            p_title = view.findViewById(R.id.p_title);
            p_description = view.findViewById(R.id.p_description);
            p_time = view.findViewById(R.id.p_time);
            like_btn = view.findViewById(R.id.like_btn);
            comment_btn = view.findViewById(R.id.comment_btn);
            share_btn = view.findViewById(R.id.share_btn);
            more_btn = view.findViewById(R.id.more_btn);
        }
    }
}
