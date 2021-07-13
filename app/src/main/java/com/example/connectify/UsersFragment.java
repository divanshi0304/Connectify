package com.example.connectify;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.connectify.Adapters.UserAdapter;
import com.example.connectify.Models.Users;
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


public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    UserAdapter user_adapter;
    List<Users> usersList;

    //firebase authorization
    FirebaseAuth firebaseAuth;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //init recycler view
        recyclerView = view.findViewById(R.id.users_rview);


        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //setting its properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init users list
        usersList = new ArrayList<>();

        //getting all users
        getUsersList();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        //for showing menu options in this fragment
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    private void checkUserStatus() {

        // getting current Users
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null) {
            //Users stays signed in here and email is set of logged in Users

        }
        else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    private void searchAllUsers(final String newText) {

        //getting current user
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //getting path of database which contains all users info
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //getting all data from path
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users muser = dataSnapshot.getValue(Users.class);

                    //getting all searched users except one who is using the app and currently signed in
                    if(!muser.getUid().equals(firebaseUser.getUid())) {

                        //if the name or email matches, add it to list
                        if(muser.getName().toLowerCase().contains(newText.toLowerCase())
                                || muser.getEmail().toLowerCase().contains(newText.toLowerCase())) {

                            usersList.add(muser);

                        }
                    }

                    //adapter
                    user_adapter = new UserAdapter(getActivity(), usersList);

                    //refresh adapter
                    user_adapter.notifyDataSetChanged();

                    //setting adapter to recycler view
                    recyclerView.setAdapter(user_adapter);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });
    }

    private void getUsersList() {

        //getting current user
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //getting path of database which contains all users info
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //getting all data from path
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {

                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users muser = dataSnapshot.getValue(Users.class);

                    //getting all users except one who is using the app and currently signed in
                    if(!muser.getUid().equals(firebaseUser.getUid())) {
                        usersList.add(muser);
                    }

                    //adapter
                    user_adapter = new UserAdapter(getActivity(), usersList);

                    //setting adapter to recycler view
                    recyclerView.setAdapter(user_adapter);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });
    }

    //inflating menu options
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hiding add post icon from this fragment
        menu.findItem(R.id.add_post).setVisible(false);

        //search view
        MenuItem menuItem = menu.findItem(R.id.search_button);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            //called when the user presses the search button
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!TextUtils.isEmpty(query.trim())) {
                    //if search query is not empty, it will search
                    searchAllUsers(query);
                }
                else {
                    //if search query is empty, get all users
                    getUsersList();
                }

                return false;
            }

            //called when user presses any letter
            @Override
            public boolean onQueryTextChange(String newText) {

                if (!TextUtils.isEmpty(newText.trim())) {
                    //if search query is not empty, it will search
                    searchAllUsers(newText);
                }
                else {
                    //if search query is empty, get all users
                    getUsersList();
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

        return super.onOptionsItemSelected(item);
    }
}