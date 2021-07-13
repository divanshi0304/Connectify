package com.example.connectify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connectify.Adapters.PostsAdapter;
import com.example.connectify.Models.Posts;
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

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    //recycler view
    RecyclerView rview_posts;

    //views from xml
    ImageView avatarpr, coverpr;
    TextView namepr, emailpr, phonepr;

    List<Posts> postsList;
    PostsAdapter postsAdapter;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init views
        avatarpr = findViewById(R.id.avatarpr);
        coverpr = findViewById(R.id.coverpr);
        namepr = findViewById(R.id.namepr);
        emailpr = findViewById(R.id.emailpr);
        phonepr = findViewById(R.id.phonepr);

        rview_posts = findViewById(R.id.rview_posts);

        firebaseAuth = FirebaseAuth.getInstance();

        //to retrive posts of clicked user, get his uid
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        //getting info of currently signed in Users
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                //checking until required data is found
                for (DataSnapshot ds : snapshot.getChildren()) {

                    //getting data
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();

                    //setting data
                    namepr.setText(name);
                    emailpr.setText(email);
                    phonepr.setText(phone);

                    //setting profile
                    try {
                        //if image is received
                        Picasso.get().load(image).into(avatarpr);
                    }
                    catch (Exception e) {
                        //if exception occurs, set it to default
                        Picasso.get().load(R.drawable.ic_add_image).into(avatarpr);
                    }

                    //setting cover picture
                    try {
                        //if image is received
                        Picasso.get().load(cover).into(coverpr);
                    }
                    catch (Exception e) {
                        //if exception occurs, set it to default
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        postsList = new ArrayList<>();

        checkUserStatus();

        loadUserPosts();

    }

    private void loadUserPosts() {

        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //loading posts from last to show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //setting this layout to recycler view
        rview_posts.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        //query to load posts
        Query query = reference.orderByChild("uid").equalTo(uid);

        //getting other data using this reference
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts myPosts = dataSnapshot.getValue(Posts.class);

                    //add to list
                    postsList.add(myPosts);

                    //adapter
                    postsAdapter = new PostsAdapter(UserProfileActivity.this, postsList);

                    //setting this adapter to recycler view
                    rview_posts.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                Toast.makeText(UserProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchUserPosts(String searchtext) {

        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //loading posts from last to show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //setting this layout to recycler view
        rview_posts.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        //query to load posts
        Query query = reference.orderByChild("uid").equalTo(uid);

        //getting other data using this reference
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts myPosts = dataSnapshot.getValue(Posts.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchtext.toLowerCase())
                            || myPosts.getpDescription().toLowerCase().contains(searchtext.toLowerCase())) {

                        //add to list
                        postsList.add(myPosts);
                    }

                    //adapter
                    postsAdapter = new PostsAdapter(UserProfileActivity.this, postsList);

                    //setting this adapter to recycler view
                    rview_posts.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                Toast.makeText(UserProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        //hiding add post button from this activity
        menu.findItem(R.id.add_post).setVisible(false);

        MenuItem item = menu.findItem(R.id.search_button);

        //search view for searching posts of a specific user
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            //called when user presses search button
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    //search posts
                    searchUserPosts(query);
                }
                else {
                    loadUserPosts();
                }
                return false;
            }

            //called when user types any letter
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    //search posts
                    searchUserPosts(newText);
                }
                else {
                    loadUserPosts();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id==R.id.logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {

        // getting current Users
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null) {
            //Users stays signed in here and email is set of logged in Users
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}