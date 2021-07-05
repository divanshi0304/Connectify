package com.example.connectify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;

    ProgressDialog dialog;

    EditText email_box, password_box;
    Button login_button, create_button;
    TextView recoverPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_acitivty);

        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Logging in...");

        email_box = findViewById(R.id.email_box);
        password_box = findViewById(R.id.password_box);
        recoverPass = findViewById(R.id.forgot_password);

        login_button = findViewById(R.id.login_button);
        create_button = findViewById(R.id.create_button);

        login_button.setOnClickListener((v) -> {
                dialog.show();
                String email, password;

                email = email_box.getText().toString();
                password = password_box.getText().toString();

                //logging in the Users
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();

                            //getting email and uid of Users from auth
                            String email = user.getEmail();
                            String uid = user.getUid();

                            //firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            //path to store Users data named "Users"
                            DatabaseReference reference = database.getReference("Users");

                            //putting data within hashmap in database
                            reference.child(uid).child("onlineStatus").setValue("online");
                            reference.child(uid).child("typingStatus").setValue("no");


                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        }
                        else {
                            Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        });

        //password recovery
        recoverPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //creating new account
        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void showRecoverPasswordDialog() {

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //setting layout as linear layout
        LinearLayout linearLayout = new LinearLayout(this);

        //views to set in dialog
        EditText emailET = new EditText(this);
        emailET.setHint("Email");
        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //setting the min width of editview to fit a text regardless of actual text size and extension
        emailET.setMinEms(16);

        linearLayout.addView(emailET);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        //recovery button
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input the email
                String email = emailET.getText().toString().trim();
                beginRecovery(email);
            }
        });

        //cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();

    }

    private void beginRecovery(String email) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                // get and show proper error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}