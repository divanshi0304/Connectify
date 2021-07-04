package com.example.connectify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.PrimitiveIterator;

public class AddPostActivity extends AppCompatActivity {
    
    //firebase authorization
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    
    //actionbar
    ActionBar actionBar;
    
    //views
    EditText post_title, post_description;
    ImageView post_image;
    Button post_upload;

    //uri for picked image
    Uri image_uri = null;
    
    //permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;

    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    
    //permissions array
    String[] camera_permissions;
    String[] storage_permissions;

    //user info
    String name, email, uid, image;

    //progress bar
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        
        //init permissions array
        camera_permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storage_permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        
        //enabling back button in it
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setSubtitle(email);

        progressDialog = new ProgressDialog(this);
        
        //init
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //getting current user's data to include in post;
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    name = "" + dataSnapshot.child("name").getValue();
                    email = "" + dataSnapshot.child("email").getValue();
                    image = "" + dataSnapshot.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        
        //init views
        post_title = findViewById(R.id.post_title);
        post_description = findViewById(R.id.post_description);
        post_image = findViewById(R.id.post_image);
        post_upload = findViewById(R.id.post_upload);
        
        //getting image on clicking (camera/gallery)
        post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                //showing image pick dialog
                showImagePickDialog();
            }
        });
        
        //click listener for upload button
        post_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getting data from edit texts
                String title = post_title.getText().toString().trim();
                String description = post_description.getText().toString().trim();

                if(TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Enter Title!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Enter Description!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(image_uri==null) {
                    //post is without image
                    uploadData(title, description, "noImage");
                }
                else {
                    //post is with an image
                    uploadData(title, description, String.valueOf(image_uri));
                }
            }
        });
    }

    private void uploadData(String title, String description, String uri) {
        progressDialog.setMessage("Publishing post");
        progressDialog.show();

        //for post's publishing time
        String timeStamp = String.valueOf(System.currentTimeMillis());

        //for post image name and id
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        if (!uri.equals("noImage")) {
            //post is with image
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            reference.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //getting url as image is uploaded
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());

                    String downloadUri = uriTask.getResult().toString();

                    if (uriTask.isSuccessful()) {

                        //url received, upload post to firebase database
                        HashMap<Object, String> hashMap = new HashMap<>();

                        //putting post information
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uImage", image);
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pTitle", title);
                        hashMap.put("pDescription", description);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime", timeStamp);

                        //path for storing data
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        //putting data in this refrence
                        reference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                //post successfully added in database
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, "Post Published!", Toast.LENGTH_SHORT).show();

                                //resetting the views
                                post_title.setText("");
                                post_description.setText("");
                                post_image.setImageURI(null);
                                image_uri=null;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NotNull Exception e) {

                                //failed to add post in database
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    //failed to upload data
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {

            //post is without image
            HashMap<Object, String> hashMap = new HashMap<>();

            //putting post information
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uImage", image);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescription", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);

            //path for storing data
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

            //putting data in this refrence
            reference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    //post successfully added in database
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post Published!", Toast.LENGTH_SHORT).show();

                    //resetting the views
                    post_title.setText("");
                    post_description.setText("");
                    post_image.setImageURI(null);
                    image_uri=null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NotNull Exception e) {

                    //failed to add post in database
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showImagePickDialog() {
        
        //options to show
        String[] options = {"Camera", "Gallery"};
        
        //dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        
        //setting options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handling item clicks
                if (which==0) {
                    //camera is chosen
                    if(!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if (which==1)
                {
                    //gallery is chosen
                    if(!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        
        //create and show dialog
        builder.create().show();
    }

    private void pickFromGallery() {

        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {

        //intent to pick image from camera
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        
        //go to previous activity
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        menu.findItem(R.id.add_post).setVisible(false);
        menu.findItem(R.id.search_button).setVisible(false);
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

    private void checkUserStatus() {

        // getting current Users
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null) {
            //Users stays signed in here and email is set of logged in Users
            email = user.getEmail();
            uid = user.getUid();


        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }
    
    private boolean checkStoragePermission() {
        //return true if enabled, otherwise false
        boolean allowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return allowed;
    }
    
    private void requestStoragePermission() {
        //requesting runtime storage permission
        ActivityCompat.requestPermissions(this, storage_permissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //return true if enabled, otherwise false
        boolean allowed = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean allowed1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        
        return allowed && allowed1;
    }

    private void requestCameraPermission() {
        //requesting runtime storage permission
        ActivityCompat.requestPermissions(this, camera_permissions, CAMERA_REQUEST_CODE);
    }

    //handling permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //handling allowed and denied permission cases

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length>0) {
                    boolean cameraAllowed = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAllowed = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAllowed && storageAllowed) {
                        //both permissions were granted
                        pickFromCamera();
                    }
                    else {
                        //both permission were not granted
                        Toast.makeText(this, "Both Camera and Storage permissions are necessary", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if(grantResults.length>0) {
                    boolean storageAllowed = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAllowed) {
                        //storage permission granted!
                        pickFromGallery();
                    }
                    else {
                        //storage permission not granted
                        Toast.makeText(this, "Storage permission necessary", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                }
            }
        }
    }

    //called after picking image from camera or gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {

        if(resultCode == RESULT_OK) {
            if(requestCode == IMAGE_PICK_GALLERY_CODE) {

                //get image uri (which was picked from gallery)
                image_uri = data.getData();

                //set it to image view
                post_image.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE) {

                //get image uri (which was picked from camera)
                post_image.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}