package com.example.connectify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.HashMap;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {

    //views
    LinearLayout profileLayout;
    ImageView uImage, pImage;
    TextView uNamepr, pTime, pTitle, pDescription, pLikespr, pCommentspr;
    ImageButton moreBtn;
    Button likeBtn, shareBtn;

    //adding views for comments
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cimagePr;

    //to get details about user and post
    String myUid, myEmail, myName, myImage, postId, pLikes, pImage_, userImage, userName, hisUid;

    //progress bar
    ProgressDialog progressDialog;

    boolean processComment = false;
    boolean processLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //defining action bar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //retrieving post id using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //init views
        uImage = findViewById(R.id.u_image);
        pImage = findViewById(R.id.p_image);
        uNamepr = findViewById(R.id.u_name);
        pTime = findViewById(R.id.p_time);
        pTitle = findViewById(R.id.p_title);
        pDescription = findViewById(R.id.p_description);
        pCommentspr = findViewById(R.id.p_comments);
        pLikespr = findViewById(R.id.p_likes);
        moreBtn = findViewById(R.id.more_btn);
        shareBtn = findViewById(R.id.share_btn);
        likeBtn = findViewById(R.id.like_btn);
        profileLayout = findViewById(R.id.profile_layout);

        commentEt = findViewById(R.id.comment_et);
        cimagePr = findViewById(R.id.c_imagepr);
        sendBtn = findViewById(R.id.send_btn);
        
        loadPostDetails();

        checkUserStatus();
        
        loadUserDetails();

        setLikes();

        //setting subtitle of action bar
        actionBar.setSubtitle("Signed-in as: " + myEmail);
        
        //handling click on send send comment button
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        //handling click on like button
        likeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        //handling click on more button
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
    }

    private void showMoreOptions() {
        //pop up menu for delete option
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        //checking whether the post is of signed in user or not
        if (hisUid.equals(myUid)) {
            //adding items in pop up menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        }

        //handling click on item
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0) {
                    //delete is chosen/clicked
                    startDelete();
                }
                return false;
            }
        });

        //showing the pop up menu
        popupMenu.show();
    }

    private void startDelete() {
        //checking whether the post is with or without image
        if (pImage_.equals("noImage")) {
            //post is without image
            deleteWithoutImage();
        }
        else {
            //post is with image
            deleteWithImage();
        }
    }

    private void deleteWithoutImage() {
        //progress bar for progress
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting post...");

        //image was deleted successfully, delete database
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    //removing desired values from database by matching posts id
                    dataSnapshot.getRef().removeValue();
                }

                //successfully deleted
                Toast.makeText(PostActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void deleteWithImage() {
        //progress bar for progress
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting post...");

        StorageReference ireference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage_);
        ireference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //image was deleted successfully, delete database
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            //removing desired values from database by matching posts id
                            dataSnapshot.getRef().removeValue();
                        }

                        //successfully deleted
                        Toast.makeText(PostActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        
    }

    private void setLikes() {
        DatabaseReference likeReference = FirebaseDatabase.getInstance().getReference().child("Likes");

        likeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)) {
                    //currently signed in user has liked the post
                    //change text and icon color of like button
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_default_liked, 0, 0, 0);
                    likeBtn.setText("Liked");

                }
                else {
                    //currently signed in user has not liked the post
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        //checking whether currently signed in user has liked or not; if not, incrementing number if likes by 1 on clicking like button
        processLike = true;

        //retrieving id of clicked post
        DatabaseReference likeReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (processLike) {
                    if (snapshot.child(postId).hasChild(myUid)) {
                        //if already liked, remove it (toggle)
                        postReference.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likeReference.child(postId).child(myUid).removeValue();
                        processLike = false;

                        /*likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                        likeBtn.setText("Like");*/
                    }
                    else {
                        //not liked yet, increment the value
                        postReference.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likeReference.child(postId).child(myUid).setValue("Liked");
                        processLike = false;

                        /*likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_default_liked, 0, 0, 0);
                        likeBtn.setText("Liked");*/
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding comment...");

        //retrieving data from comment edit text
        String comment = commentEt.getText().toString().trim();

        //validating it
        if (TextUtils.isEmpty(comment)) {
            //when no value is entered
            Toast.makeText(this, "Comment is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        //putting comment details in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uImage", myImage);
        hashMap.put("uName", myName);

        //putting this data in database
        reference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //added successfully
                Toast.makeText(PostActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentsCount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                //failed
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCommentsCount() {
        //on adding a comment, increment the number by one
        processComment = true;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (processComment) {
                    String comments = "" + snapshot.child("pComments").getValue();
                    int newCommentNum = Integer.parseInt(comments) + 1;
                    reference.child("pComments").setValue("" + newCommentNum);
                    processComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadUserDetails() {
        //getting current user's details
        Query query = FirebaseDatabase.getInstance().getReference("USers");
        query.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    myName = "" + dataSnapshot.child("name").getValue();
                    myImage = "" + dataSnapshot.child("image").getValue();

                    //setting data
                    try {
                        Picasso.get().load(myImage).placeholder(R.drawable.ic_default_user).into(cimagePr);
                    }
                    catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_user).into(cimagePr);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadPostDetails() {
        //retrieving post using post id
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                //keep checking posts until post id matches
                for (DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    //getting data;
                    String pTitle_ = "" + dataSnapshot.child("pTitle").getValue();
                    String pDescription_ = "" + dataSnapshot.child("pDescription").getValue();
                    pLikes = "" + dataSnapshot.child("pLikes").getValue();
                    String pTimeStamp_ = "" + dataSnapshot.child("pTime").getValue();
                    pImage_ = "" + dataSnapshot.child("pImage").getValue();
                    userImage = "" + dataSnapshot.child("uImage").getValue();
                    hisUid = "" + dataSnapshot.child("uid").getValue();
                    String uEmail_ = "" + dataSnapshot.child("uEmail").getValue();
                    userName = "" + dataSnapshot.child("uName").getValue();
                    String commentNum = "" + dataSnapshot.child("pComments").getValue();


                    //converting timestamp to desired time format
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    try {
                        calendar.setTimeInMillis(Long.parseLong(pTimeStamp_));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    String pTime_ = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //setting data
                    pTitle.setText(pTitle_);
                    pDescription.setText(pDescription_);
                    pLikespr.setText(pLikes + " Likes");
                    pTime.setText(pTime_);
                    pCommentspr.setText(commentNum + " Comments");
                    uNamepr.setText(userName);

                    //setting image of user who posted
                    //setting post image; if no image, hide image view
                    if (pImage.equals("noImage"))
                    {
                        //hiding image view
                        pImage.setVisibility(View.GONE);
                    }
                    else {
                        //hiding image view
                        pImage.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage_).into(pImage);
                        } catch (Exception exception) {

                        }
                    }

                    //setting image of user in comments section
                    try {
                        Picasso.get().load(userImage).placeholder(R.drawable.ic_default_user).into(uImage);
                    }
                    catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_user).into(uImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {

        // getting current Users
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null) {
            //Users stays signed in here and email is set of logged in Users
            myUid = user.getUid();
            myEmail = user.getEmail();

        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //hiding add post and search user icons
        menu.findItem(R.id.add_post).setVisible(false);
        menu.findItem(R.id.search_button).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //getting item id
        int id = item.getItemId();
        if(id==R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}