package com.example.connectify.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.Models.Posts;
import com.example.connectify.PostActivity;
import com.example.connectify.R;
import com.example.connectify.UserProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    //post list to be displayed
    Context context;
    List<Posts> postsList;
    String myUid;

    private DatabaseReference likesReference;
    private DatabaseReference postsReference;

    boolean processLike=false;

    //constructor
    public PostsAdapter(Context context, List<Posts> postsList) {
        this.context = context;
        this.postsList = postsList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        //inflating posts row layout
        View view = LayoutInflater.from(context).inflate(R.layout.posts_row, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull PostsAdapter.MyHolder holder, int position) {

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
        String pLikes = postsList.get(position).getpLikes();
        String pComments = postsList.get(position).getpComments();

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
        holder.p_likes.setText(pLikes + " Likes");
        holder.p_comments.setText(pComments + " Comments");

        //setting likes for each post
        setLikes(holder, pId);

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
            //hiding image view
            holder.p_image.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(holder.p_image);
            } catch (Exception exception) {

            }
        }

        //handling button clicks
        holder.more_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.more_btn, uid, myUid, pId, pImage);
            }
        });

        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //checking whether currently signed in user has liked or not; if not, incrementing number if likes by 1 on clicking like button
                int pLikes = Integer.parseInt(postsList.get(position).getpLikes());
                processLike = true;

                //retrieving id of clicked post
                final String postId = postsList.get(position).getpId();
                likesReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (processLike) {
                            if (snapshot.child(postId).hasChild(myUid)) {
                                //if already liked, remove it (toggle)
                                postsReference.child(postId).child("pLikes").setValue(""+(pLikes-1));
                                likesReference.child(postId).child(myUid).removeValue();
                                processLike = false;
                            }
                            else {
                                //not liked yet, increment the value
                                postsReference.child(postId).child("pLikes").setValue(""+(pLikes+1));
                                likesReference.child(postId).child(myUid).setValue("Liked");
                                processLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        holder.comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //starting post activity
                Intent intent = new Intent(context, PostActivity.class);

                //getting details of post to expand using post id
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        holder.share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
            }
        });

        holder.profile_layout.setOnClickListener(new View.OnClickListener() {

            //on clicking a user, go to his profile activity using uid
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

    }

    private void setLikes(MyHolder holder, final String pId) {
        likesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(pId).hasChild(myUid)) {
                    //currently signed in user has liked the post
                    //change text and icon color of like button
                    holder.like_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_default_liked, 0, 0, 0);
                    holder.like_btn.setText("Liked");

                }
                else {
                    //currently signed in user has not liked the post
                    holder.like_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    holder.like_btn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton more_btn, String uid, String myUid, String pId, String pImage) {

        //pop up menu for delete option
        PopupMenu popupMenu = new PopupMenu(context, more_btn, Gravity.END);

        //checking whether the post is of signed in user or not
        if (uid.equals(myUid)) {
            //adding items in pop up menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        }

        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Details");

        //handling click on item
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0) {
                    //delete is chosen/clicked
                    startDelete(pId, pImage);
                }
                else if(id==2) {
                    //starting post activity
                    Intent intent = new Intent(context, PostActivity.class);

                    //getting details of post to expand using post id
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });

        //showing the pop up menu
        popupMenu.show();
    }

    private void startDelete(String pId, String pImage) {
        //checking whether the post is with or without image
        if (pImage.equals("noImage")) {
            //post is without image
            deleteWithoutImage(pId);
        }
        else {
            //post is with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithoutImage(String pId) {

        //progress bar for progress
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting post...");

        //image was deleted successfully, delete database
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    //removing desired values from database by matching posts id
                    dataSnapshot.getRef().removeValue();
                }

                //successfully deleted
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    private void deleteWithImage(String pId, String pImage) {

        //progress bar for progress
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting post...");

        StorageReference ireference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        ireference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //image was deleted successfully, delete database
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            //removing desired values from database by matching posts id
                            dataSnapshot.getRef().removeValue();
                        }

                        //successfully deleted
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NotNull Exception e) {
                //failed to delete image
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
        TextView u_name, p_title, p_description, p_time, p_likes, p_comments;
        Button like_btn, comment_btn, share_btn;
        ImageButton more_btn;
        LinearLayout profile_layout;

        public MyHolder(@NonNull View view) {

            super(view);

            //init views
            u_image = view.findViewById(R.id.u_image);
            p_image = view.findViewById(R.id.p_image);
            u_name = view.findViewById(R.id.u_name);
            p_title = view.findViewById(R.id.p_title);
            p_description = view.findViewById(R.id.p_description);
            p_time = view.findViewById(R.id.p_time);
            p_likes = view.findViewById(R.id.p_likes);
            p_comments = view.findViewById(R.id.p_comments);
            like_btn = view.findViewById(R.id.like_btn);
            comment_btn = view.findViewById(R.id.comment_btn);
            share_btn = view.findViewById(R.id.share_btn);
            more_btn = view.findViewById(R.id.more_btn);
            profile_layout = view.findViewById(R.id.profile_layout);

        }
    }
}
