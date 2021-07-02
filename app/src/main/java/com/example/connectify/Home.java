package com.example.connectify;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Home extends Fragment {

    //firebase authorization
    FirebaseAuth firebaseAuth;

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
        inflater.inflate(R.menu.menu_main, menu);
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