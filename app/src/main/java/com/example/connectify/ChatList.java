package com.example.connectify;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.connectify.Adapters.AChatList;
import com.example.connectify.Adapters.UserAdapter;
import com.example.connectify.Models.Chats;
import com.example.connectify.Models.MChatList;
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


public class ChatList extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;

    //declaring views
    RecyclerView chatlist_rview;

    List<MChatList> mChatLists;
    AChatList aChatList;
    List<Users> usersList;

    DatabaseReference reference;
    FirebaseUser currentUser;

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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //recycler view
        chatlist_rview = view.findViewById(R.id.chatlist_rview);

        mChatLists = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                mChatLists.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    MChatList chatList = dataSnapshot.getValue(MChatList.class);
                    mChatLists.add(chatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {

        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    for (MChatList chatList: mChatLists) {
                        if (users.getUid() != null && users.getUid().equals(chatList.getId())) {
                            usersList.add(users);
                            break;
                        }
                    }

                    //adapter
                    aChatList = new AChatList(getContext(), usersList);

                    //setting adapter
                    chatlist_rview.setAdapter(aChatList);

                    //setting last message
                    for (int i = 0; i < usersList.size(); i++) {
                        lastMessage(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                String lastMessage = "default";
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    if (chats==null) {
                        continue;
                    }
                    String sender = chats.getSender();
                    String receiver = chats.getReceiver();
                    if (sender==null || receiver==null) {
                        continue;
                    }
                    if (chats.getReceiver().equals(currentUser.getUid()) && chats.getSender().equals(userId) ||
                    chats.getReceiver().equals(userId) && chats.getSender().equals(currentUser.getUid())) {
                        lastMessage = chats.getMessage();
                    }
                }

                aChatList.setLastMessageMap(userId, lastMessage);
                aChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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