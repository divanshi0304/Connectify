package com.example.connectify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;

import java.net.MalformedURLException;
import java.net.URL;


public class Video extends Fragment {

    //firebase authorization
    FirebaseAuth firebaseAuth;

    //jitsi meet view
    private JitsiMeetView jitsiMeetView;

    //declaration of secret code and buttons
    EditText secret_code;
    Button join_button, share_button;

    public Video() {
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
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        //init views
        secret_code = view.findViewById(R.id.secret_code);
        join_button = (Button) view.findViewById(R.id.join_button);
        share_button = (Button) view.findViewById(R.id.share_button);

        //URL variable declaration
        URL server_url;

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //connecting to jitsi meet
        try {
            server_url = new URL("https://meet.jit.si");

            JitsiMeetConferenceOptions default_options =
                    new JitsiMeetConferenceOptions.Builder()
                            .setServerURL(server_url).
                            setWelcomePageEnabled(false).build();
            JitsiMeet.setDefaultConferenceOptions(default_options);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final Context context = getActivity();


        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                        .setRoom(secret_code.getText().toString())
                        .setWelcomePageEnabled(false).build();

                JitsiMeetActivity.launch(context, options);

            }
        });
        //return inflater.inflate(R.layout.fragment_video, container, false);
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