package com.example.connectify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class DashboardActivity extends AppCompatActivity {

    //firebase authorization
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Actionbar and title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //bottom navigation
        BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //transaction for home fragment which will be default on starting the app

        //changing actionbar title
        actionBar.setTitle("Home");
        Home fragment1 = new Home();
        FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
        fragmentTransaction1.replace(R.id.content, fragment1, "");
        fragmentTransaction1.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {

                    //handling item clicks
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            //transaction for home fragment

                            //changing actionbar title
                            actionBar.setTitle("Home");
                            Home fragment1 = new Home();
                            FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction1.replace(R.id.content, fragment1, "");
                            fragmentTransaction1.commit();
                            return true;

                        case R.id.navigation_users:
                            //transaction for users fragment

                            //changing actionbar title
                            actionBar.setTitle("Users");
                            UsersFragment fragment2 = new UsersFragment();
                            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction2.replace(R.id.content, fragment2, "");
                            fragmentTransaction2.commit();
                            return true;


                        case R.id.navigation_video:
                            //transaction for video call fragment

                            //changing actionbar title
                            actionBar.setTitle("Video call");
                            Video fragment3 = new Video();
                            FragmentTransaction fragmentTransaction3 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction3.replace(R.id.content, fragment3, "");
                            fragmentTransaction3.commit();
                            return true;

                        case R.id.navigation_chat:
                            //transaction for profile fragment

                            //changing actionbar title
                            actionBar.setTitle("Chats");
                            ChatList fragment4 = new ChatList();
                            FragmentTransaction fragmentTransaction4 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction4.replace(R.id.content, fragment4, "");
                            fragmentTransaction4.commit();
                            return true;


                        case R.id.navigation_profile:
                            //transaction for profile fragment

                            //changing actionbar title
                            actionBar.setTitle("Profile");
                            Profile fragment5 = new Profile();
                            FragmentTransaction fragmentTransaction5 = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction5.replace(R.id.content, fragment5, "");
                            fragmentTransaction5.commit();
                            return true;

                    }
                    return false;
                }
            };

    private void checkUserStatus() {

        // getting current Users
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null) {
            //Users stays signed in here and email is set of logged in Users
            //myProfileTv.setText(Users.getEmail());

        }
        else {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //checking on start of app
        checkUserStatus();
        super.onStart();
    }

}