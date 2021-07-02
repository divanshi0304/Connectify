package com.example.connectify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.connectify.Models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore database;

    ProgressDialog dialog;

    EditText email_box, password_box, name_box;
    Button login_button, create_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Registering User...");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        name_box = findViewById(R.id.name_box);
        email_box = findViewById(R.id.email_box);
        password_box = findViewById(R.id.password_box);

        login_button = findViewById(R.id.login_button);
        create_button = findViewById(R.id.create_button);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, name;

                email = email_box.getText().toString();
                password = password_box.getText().toString();
                name = name_box.getText().toString();

                final Users person = new Users();
                person.setName(name);
                person.setEmail(email);
                person.setPassword(password);

                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            database.collection("users").document().set(person).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseUser user = auth.getCurrentUser();

                                    //getting email and uid of Users from auth
                                    String email = user.getEmail();
                                    String uid = user.getUid();

                                    //save information of Users in firebase using hashmap
                                    HashMap<Object, String> hashMap = new HashMap<>();

                                    //putting information in hashmap
                                    hashMap.put("email", email);
                                    hashMap.put("uid", uid);
                                    hashMap.put("name", "");
                                    hashMap.put("image", "");
                                    hashMap.put("phone", "");
                                    hashMap.put("cover", "");
                                    hashMap.put("onlineStatus", "online");

                                    //firebase database instance
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                                    //path to store Users data named "Users"
                                    DatabaseReference reference = database.getReference("Users");

                                    //putting data within hashmap in database
                                    reference.child(uid).setValue(hashMap);

                                    Toast.makeText(SignupActivity.this, "Registered! \n"+user.getEmail(), Toast.LENGTH_SHORT).show();

                                    //redirecting the Users to login page
                                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                }
                            });
                            Toast.makeText(SignupActivity.this, "Account is created.",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(SignupActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        //Go to previous activity
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}