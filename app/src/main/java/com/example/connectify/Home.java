package com.example.connectify;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.service.autofill.Dataset;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.connectify.Adapters.PostsAdapter;
import com.example.connectify.Models.Posts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class Home extends Fragment {

    //firebase authorization
    FirebaseAuth firebaseAuth;

    //declaring views
    RecyclerView recyclerView;

    List<Posts> postsList;
    PostsAdapter postsAdapter;

    public Home() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        //for showing menu options in this fragment
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //recycler view and its properties
        recyclerView = view.findViewById(R.id.post_rview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //to show latest post first, loading data from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //setting layout to recycler view
        recyclerView.setLayoutManager(layoutManager);

        //init posts
        postsList = new ArrayList<>();

        loadPosts();

        return view;
    }


    private void checkUserStatus() {

        // getting current Users
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null) {
            //Users stays signed in here and email is set of logged in Users
            //myProfileTv.setText(Users.getEmail());

        }
        else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    //inflating menu options
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //adding search view to search posts by title or description
        MenuItem item = menu.findItem(R.id.search_button);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            //called when user presses search button
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query);
                }
                else {
                    loadPosts();
                }
                return false;
            }

            //called when user presses any letter
            @Override
            public boolean onQueryTextChange(String query) {

                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query);
                }
                else {
                    loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    //handling menu items on clicking
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //get item id
        int id = item.getItemId();
        if(id==R.id.logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }

        if(id==R.id.add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPosts() {

        //path of all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        //getting all data from this reference
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {

                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts posts = dataSnapshot.getValue(Posts.class);

                    //adding posts
                    postsList.add(posts);

                    //adapter
                    postsAdapter = new PostsAdapter(getActivity(), postsList);

                    //setting adapter to recycler view
                    recyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

                //if error occurs
                //Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void searchPosts(final String query) {

        //path of all posts
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        //getting all data from this reference
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts posts = dataSnapshot.getValue(Posts.class);

                    //adding posts
                    if (posts.getpTitle().toLowerCase().contains(query.toLowerCase())
                            || posts.getpDescription().toLowerCase().contains(query.toLowerCase())) {
                        postsList.add(posts);

                    }

                    //adapter
                    postsAdapter = new PostsAdapter(getActivity(), postsList);

                    //setting adapter to recycler view
                    recyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

                //if error occurs
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}