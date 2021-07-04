package com.example.connectify;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connectify.Adapters.PostsAdapter;
import com.example.connectify.Models.Posts;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.firestore.FirebaseFirestore.getInstance;

public class Profile extends Fragment {

    //firebase initialization
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //firebase storage
    StorageReference storageReference;

    //path to store Users profile and and cover pictures
    String storagePath = "Users_Profile_Cover_Imgs/";

    //views from xml
    ImageView avatarpr, coverpr;
    TextView namepr, emailpr, phonepr;
    FloatingActionButton float_button;
    RecyclerView rview_posts;

    //progress dialog box
    ProgressDialog progressDialog;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //check profile or cover pictures
    String profileOrCoverPhoto;

    //arrays for permissions to be requested
    String camera_per[];
    String storage_per[];

    List<Posts> postsList;
    PostsAdapter postsAdapter;
    String uid;

    //uri of picked image
    Uri image_uri;


    public Profile() {
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        //init array of permissions
        camera_per = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storage_per = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatarpr = view.findViewById(R.id.avatarpr);
        coverpr = view.findViewById(R.id.coverpr);
        namepr = view.findViewById(R.id.namepr);
        emailpr = view.findViewById(R.id.emailpr);
        phonepr = view.findViewById(R.id.phonepr);
        float_button = view.findViewById(R.id.float_button);
        rview_posts = view.findViewById(R.id.rview_posts);

        //init progress dialog box
        progressDialog = new ProgressDialog(getActivity());

        //getting info of currently signed in Users
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                //checking until required data is found
                for (DataSnapshot ds : snapshot.getChildren()) {

                    //getting data
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();

                    //setting data
                    namepr.setText(name);
                    emailpr.setText(email);
                    phonepr.setText(phone);

                    //setting profile
                    try {
                        //if image is received
                        Picasso.get().load(image).into(avatarpr);
                    }
                    catch (Exception e) {
                        //if exception occurs, set it to default
                        Picasso.get().load(R.drawable.ic_add_image).into(avatarpr);
                    }

                    //setting cover picture
                    //setting image
                    try {
                        //if image is received
                        Picasso.get().load(cover).into(coverpr);
                    }
                    catch (Exception e) {
                        //if exception occurs, set it to default
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //clicking on floating action button
        float_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postsList = new ArrayList<>();

        //checking whether the user is signed in or not
        checkUserStatus();

        //to load all posts of a particular user
        loadMyPosts();

        return view;
    }

    private void loadMyPosts() {

        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //loading posts from last to show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //setting this layout to recycler view
        rview_posts.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        //query to load posts
        Query query = reference.orderByChild("uid").equalTo(uid);

        //getting other data using this reference
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts myPosts = dataSnapshot.getValue(Posts.class);
                    
                    //add to list
                    postsList.add(myPosts);
                    
                    //adapter
                    postsAdapter = new PostsAdapter(getActivity(), postsList);
                    
                    //setting this adapter to recycler view
                    rview_posts.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchMyPosts(String searchtext) {

        //linear layout for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //loading posts from last to show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //setting this layout to recycler view
        rview_posts.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        //query to load posts
        Query query = reference.orderByChild("uid").equalTo(uid);

        //getting other data using this reference
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts myPosts = dataSnapshot.getValue(Posts.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchtext.toLowerCase())
                    || myPosts.getpDescription().toLowerCase().contains(searchtext.toLowerCase())) {

                        //add to list
                        postsList.add(myPosts);
                    }

                    //adapter
                    postsAdapter = new PostsAdapter(getActivity(), postsList);

                    //setting this adapter to recycler view
                    rview_posts.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkStoragePermission() {

        //checking if storage permission is enabled or not
        boolean permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return permission;
    }

    private void requestStoragePermission() {

        //requesting run-time storage permission
        requestPermissions(storage_per, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {

        //checking if storage permission is enabled or not
        boolean permission1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean permission2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return permission1 && permission2;
    }

    private void requestCameraPermission() {

        //requesting run-time storage permission
        requestPermissions(camera_per, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {

        //options which will be displayed in dialog
        String options[] = {"Edit Name", "Edit Profile Picture", "Edit Cover Picture", "Edit Phone"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //setting title
        builder.setTitle("Choose Action");

        //setting items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //handling dialogs on clicking
                if (which == 0) {
                    //edit name
                    progressDialog.setMessage("Updating name");

                    //passing key "name" as parameter to a method to update it's value in database
                    showNamePhoneUpdateDialog("name");
                }
                else if (which == 1) {
                    //edit profile picture
                    progressDialog.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePictureDialog();
                }
                else if (which == 2) {
                    //edit cover picture
                    progressDialog.setMessage("Updating Cover Picture");
                    profileOrCoverPhoto = "cover";
                    showImagePictureDialog();
                }
                else if (which == 3) {
                    //edit phone number
                    progressDialog.setMessage("Updating Phone Number");

                    //passing key "phone" as parameter to a method to update it's value in database
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });

        //create and show dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(String key) {

        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);

        //setting layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        //add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //adding button in dialog to update
        builder.setPositiveButton("Update ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //input text from edit text
                String value = editText.getText().toString().trim();

                //validating whether the Users has entered something or not
                if(!TextUtils.isEmpty(value)) {
                    progressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    //updated so dismiss the progress dialog
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {

                                    //an error occurred, get and display it; also dismiss the dialog
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    //when user changes name, change it from posts as well
                    if (key.equals("name")) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = databaseReference.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String child = dataSnapshot.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                    else {
                        Toast.makeText(getActivity(), "Please enter "+key, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //adding button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        //create and show dialog
        builder.create().show();

    }

    private void showImagePictureDialog() {

        //show dialog containing two options to pick the image (Camera and gallery)

        //options which will be displayed in dialog
        String options[] = {"Camera", "Gallery"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //setting title
        builder.setTitle("Choose image from");

        //setting items to dialog
        builder.setItems(options, (dialog, which) -> {

                //handling dialogs on clicking
             if (which == 0) {
                 //Camera clicked

                 if(!checkCameraPermission()) {
                     requestCameraPermission();
                 }
                 else {
                     pickFromCamera();
                 }
             }
             else if (which == 1) {
                 //Gallery clicked
                 if(!checkStoragePermission()) {
                     requestStoragePermission();
                 }
                 else {
                     pickFromGallery();
                 }
             }
        });

        //create and show dialog
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {

        //handling permissions when the Users clicks allow or deny
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {

                if(grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted) {
                        //permission is given
                        pickFromCamera();
                    }
                    else {
                        //permission is denied
                        Toast.makeText(getActivity(), "Please enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {

                if(grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted) {
                        //permission is given
                        pickFromGallery();
                    }
                    else {
                        //permission is denied
                        Toast.makeText(getActivity(), "Please enable storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {

        if(resultCode == RESULT_OK) {
            if(requestCode == IMAGE_PICK_GALLERY_CODE) {

                //image is chosen from gallery and uploaded
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);

            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE) {

                //image is chosen from camera and uploaded

                uploadProfileCoverPhoto(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri image_uri) {

        //showing progress
        progressDialog.show();

        //path ans name of image to be stored in firebase storage
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference1 = storageReference.child(filePathAndName);
        storageReference1.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //getting uploaded image's url and store in Users's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //checking whether is uploaded or not and url is received
                        if (uriTask.isSuccessful()) {

                            // image is uploaded, add or update its url in Users's database
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //url is added successfully so dismiss progress bar
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            //error occurred while adding the url in database so dismiss progress bar
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating ", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            //when user changes his name, change it from posts as well
                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = databaseReference.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            String child = dataSnapshot.getKey();
                                            snapshot.getRef().child(child).child("uImage").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        else {
                            //error occurs
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                        //if error occurs, get and show them, and dismiss progress box
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromCamera() {

        //picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temporary picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temporary Description");

        //putting image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {

        //picking image from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void checkUserStatus() {

        // getting current Users
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null) {
            //Users stays signed in here and email is set of logged in Users
            //myProfileTv.setText(Users.getEmail());
            //getting signed in user's uid
            uid = user.getUid();

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

        MenuItem item = menu.findItem(R.id.search_button);

        //search view for searching posts of a specific user
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            //called when user presses search button
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    //search posts
                    searchMyPosts(query);
                }
                else {
                    loadMyPosts();
                }
                return false;
            }

            //called when user types any letter
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    //search posts
                    searchMyPosts(newText);
                }
                else {
                    loadMyPosts();
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

        if(id==R.id.add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}