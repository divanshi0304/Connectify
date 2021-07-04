package com.example.connectify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connectify.Adapters.ChatsAdapter;
import com.example.connectify.Models.Chats;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    //declaring views
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView imagepr;
    TextView namepr, user_status;
    EditText message_edit;
    ImageButton send_button;

    //firebase auth
    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userdatabaseReference;

    //to check is user have seen messages or not
    ValueEventListener valueListenerSeen;
    DatabaseReference userReferenceSeen;

    List<Chats> chatsList;
    ChatsAdapter chatsAdapter;

    //String for storing sender and receiver's uid
    String userUid;
    String myUid;

    String user_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init views from xml file
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView = findViewById(R.id.chat_rview);
        imagepr = findViewById(R.id.imagepr);
        namepr = findViewById(R.id.namepr);
        user_status = findViewById(R.id.user_status);
        message_edit = findViewById(R.id.message_edit);
        send_button = findViewById(R.id.send_button);

        //linear layout for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recycler view properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //getting user's uid to get profile picture, name and start conversation
        Intent intent = getIntent();
        userUid = intent.getStringExtra("userUid");

        //getting firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        //getting database reference from path "Users"
        firebaseDatabase = FirebaseDatabase.getInstance();
        userdatabaseReference = firebaseDatabase.getReference("Users");

        //searching to get that user's data
        Query query = userdatabaseReference.orderByChild("uid").equalTo(userUid);

        //getting user's name and picture
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //checking until required data is received
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    //getting data
                    String name = "" + dataSnapshot.child("name").getValue();
                    user_image = "" + dataSnapshot.child("image").getValue();

                    //getting value of online status
                    String onlineStatus = ""+dataSnapshot.child("onlineStatus").getValue();
                    if (onlineStatus.equals("online")) {
                        user_status.setText(onlineStatus);
                    }
                    else {
                        //converting time stamp to desired format
                        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                        try {
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        String dateTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                        user_status.setText("Last seen at: " + dateTime);
                    }

                    //setting data
                    namepr.setText(name);
                    try {
                        //if image received, set it to imageview in toolbar
                        Picasso.get().load(user_image).placeholder(R.drawable.ic_user_white).into(imagepr);
                    }
                    catch (Exception exception) {
                        //if exception is there, set default image
                        Picasso.get().load(R.drawable.ic_user_white).into(imagepr);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //sending message when send button is clicked
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getting text from edit text
                String message = message_edit.getText().toString().trim();

                //checking if the text is empty or not
                if (TextUtils.isEmpty(message)) {
                    //text is empty
                    Toast.makeText(ChatActivity.this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
                }
                else {
                    //text is not empty
                    sendMessage(message);
                }
            }
        });

        //linearLayoutManager.setReverseLayout(true);

        readMessages();

        seenMessages();
    }

    private void seenMessages() {

        userReferenceSeen = FirebaseDatabase.getInstance().getReference("Chats");

        valueListenerSeen = userReferenceSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chats = dataSnapshot.getValue(Chats.class);

                    if (chats.getReceiver().equals(myUid) && chats.getSender().equals(userUid)) {
                        HashMap<String, Object> seenHashMap = new HashMap<>();
                        seenHashMap.put("isSeen", true);
                        dataSnapshot.getRef().updateChildren(seenHashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {

        chatsList = new ArrayList<>();
        DatabaseReference dreference = FirebaseDatabase.getInstance().getReference("Chats");

        dreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {

                chatsList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    assert chats != null;
                    if (chats.getReceiver().equals(firebaseAuth.getUid()) && chats.getSender().equals(userUid)
                    || chats.getReceiver().equals(userUid) && chats.getSender().equals(firebaseAuth.getUid())) {

                        //add it to the chats list
                        chatsList.add(chats);
                    }

                    //adapter
                    chatsAdapter = new ChatsAdapter(ChatActivity.this, chatsList, user_image);
                    chatsAdapter.notifyDataSetChanged();

                    //set adapter to recycler view
                    recyclerView.setAdapter(chatsAdapter);
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        //putting necessary data in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", userUid);
        hashMap.put("message", message);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("isSeen", false);

        databaseReference.child("Chats").push().setValue(hashMap);
        //reset edit text after sending message
        message_edit.setText("");
    }

    @Override
    protected void onStart() {
        checkUserStatus();

        //setting online status
        checkOnlineStatus("online");

        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //getting time stamp
        String timeStamp = String.valueOf(System.currentTimeMillis());

        //setting status offline with time stamp as last seen
        checkOnlineStatus(timeStamp);
        userReferenceSeen.removeEventListener(valueListenerSeen);
    }

    @Override
    protected void onResume() {
        //setting online status
        checkOnlineStatus("online");

        super.onResume();
    }

    private void checkUserStatus() {

        // getting current Users
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();

        if(user!=null) {
            //Users stays signed in here and email is set of logged in Users
            myUid = user.getUid();

        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);

        //putting online status in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        //updating it in database
        databaseReference.updateChildren(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        //hiding search view and add posts, as it is not needed
        menu.findItem(R.id.search_button).setVisible(false);
        menu.findItem(R.id.add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //get item id
        int id = item.getItemId();
        if(id==R.id.logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}