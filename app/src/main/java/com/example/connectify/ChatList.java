package com.example.connectify;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

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


public class ChatList extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;

    public ChatList() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        //for showing menu options in this fragment
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        return view;
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

    //inflating menu options
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hiding add post icon from this fragment
        menu.findItem(R.id.add_post).setVisible(false);

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