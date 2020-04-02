package com.bibhas.handsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bibhas.handsapp.Adapters.FrindsListAdapter;
import com.bibhas.handsapp.MainActivity;
import com.bibhas.handsapp.Models.Friends;
import com.bibhas.handsapp.Models.Users;
import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Util.SharedData;
import com.bibhas.handsapp.Util.SharedMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class CreateGroupActivity extends AppCompatActivity implements FrindsListAdapter.ClickListener {

    private static final String TAG = "CreateGroupActivity";
    private ArrayList<Users> friendList = new ArrayList<>();
    private ArrayList<String> friendIdList = new ArrayList<>();
    private ArrayList<String> selectedFriendIdList = new ArrayList<>();
    private DatabaseReference usersDatabase;
    private DatabaseReference friendsDatabase;
    private DatabaseReference groupDatabase;
    private DatabaseReference chatDatabase;
    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;
    private CircleImageView mDisplayImage;

    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    private RecyclerView recyclerView;
    private CardView empty_list;
    private Button create_group;
    private EditText group_name;
    private EditText group_info;
    private TextView selected_members_no;
    private static final int GALLERY_PICK = 1111;
    private String group_id;
    private ProgressBar progressBar;
    private Bitmap thumb_bitmap;
    private Uri resultUri;
    private boolean isImageSelected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        setUpAppBar();

        InitializeFields();

        setRecyclerView();

        getFriensIdList();
    }

    private void setUpAppBar() {
        Toolbar mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void InitializeFields() {
        empty_list = findViewById(R.id.empty_list);
        create_group = findViewById(R.id.create_group);
        group_name = findViewById(R.id.group_name);
        group_info = findViewById(R.id.group_info);
        selected_members_no = findViewById(R.id.selected_members_no);
        mDisplayImage = findViewById(R.id.user_single_image);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        groupDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
        chatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");
        mImageStorage = FirebaseStorage.getInstance().getReference();

        group_id = SharedData.group_id_prefix + SharedMethods.randomAlphaNumeric();
        //Toast.makeText(this, group_id, Toast.LENGTH_SHORT).show();

        setButtonClickListener();
    }


    private void setRecyclerView() {
        recyclerView = findViewById(R.id.friend_list);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        FrindsListAdapter mAdapter = new FrindsListAdapter(friendList);
        mAdapter.setClickListener(this);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
    }

    private void getFriensIdList() {
        friendsDatabase.child(mCurrent_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            if (postSnapshot.exists()) {
                                //Log.d("2222", postSnapshot.getKey());
                                friendIdList.add(postSnapshot.getKey());
                            }
                        }
                        getFriendsDetails();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFriendsDetails() {
        if (friendIdList.size() > 0) {
            for (final String id : friendIdList) {
                usersDatabase.child(id).addValueEventListener(new ValueEventListener() {
                    String name = "";
                    String thumb_img_url = "";
                    String status = "";

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            name = dataSnapshot.child("name").getValue().toString();
                            thumb_img_url = dataSnapshot.child("thumb_image").getValue().toString();
                            status = dataSnapshot.child("status").getValue().toString();
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.getMessage());
                        }
                        Users users = new Users(id, name, null, status, thumb_img_url,null,null,null);
                        friendList.add(users);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(CreateGroupActivity.this, "Error getting friend list. please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //Log.d(TAG, friendIdList.toString());
            //Log.d(TAG, friendList.toString());
            progressBar.setVisibility(View.GONE);
            //recyclerView.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            empty_list.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonClickListener() {
        // chose image for group
        mDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

        // create group
        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = group_name.getText().toString().trim();
                String info = group_info.getText().toString().trim();
                if (!name.isEmpty()) {
                    if (SharedMethods.validateName(name)) {
                        if (selectedFriendIdList.size() > 0) {
                            //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                            //check if user already have group with same name
                            if (!info.isEmpty()) {
                                createGroup(name, info);
                            } else {
                                createGroup(name, "");
                            }

                        } else {
                            group_name.setError("Please select group member");
                        }
                    } else {
                        group_name.setError("Invalid group name. Ex:My world");
                    }
                } else {
                    group_name.setError("Group name should not be empty");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void itemClicked(View view, int position) {

        String id = friendList.get(position).getId();
        //Toast.makeText(view.getContext(), friendList.toString(), Toast.LENGTH_SHORT).show();
        if (!selectedFriendIdList.contains(id)) {
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            selectedFriendIdList.add(id);
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.transparent));
            selectedFriendIdList.remove(id);
        }

        selected_members_no.setText(String.valueOf(selectedFriendIdList.size()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
            //Toast.makeText(SettingsActivity.this, imageUri, Toast.LENGTH_LONG).show();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());

                thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                Picasso.with(CreateGroupActivity.this).load(resultUri).placeholder(R.drawable.default_avatar).into(mDisplayImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        isImageSelected = true;
                    }
                    @Override
                    public void onError() {}
                });

//
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                final byte[] thumb_byte = baos.toByteArray();
//
//                StorageReference filepath = mImageStorage.child("group_images").child(group_id + ".jpg");
//                final StorageReference thumb_filepath = mImageStorage.child("group_images").child("thumbs").child(group_id + ".jpg");
//
//                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            final String download_url = task.getResult().getDownloadUrl().toString();
//                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
//
//                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
//                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
//                                    if (thumb_task.isSuccessful()) {
//                                        Map update_hashMap = new HashMap();
//                                        update_hashMap.put("image", download_url);
//                                        update_hashMap.put("thumb_image", thumb_downloadUrl);
//
//                                        groupDatabase.child(group_id).updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()) {
//                                                    mProgressDialog.dismiss();
//                                                    Toast.makeText(CreateGroupActivity.this, "Successfully Uploaded.", Toast.LENGTH_LONG).show();
//                                                }
//                                            }
//                                        });
//
//                                    } else {
//                                        Toast.makeText(CreateGroupActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_LONG).show();
//                                        mProgressDialog.dismiss();
//                                    }
//                                }
//                            });
//                        } else {
//                            Toast.makeText(CreateGroupActivity.this, "Error in uploading.", Toast.LENGTH_LONG).show();
//                            mProgressDialog.dismiss();
//                        }
//                    }
//                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    /**
     * create groups
     * add group id to chat
     * add group id to selected users
     * modify message
     **/
    private void createGroup(String group_name, String group_info) {

        if (group_id != null && isImageSelected) {

            mProgressDialog = new ProgressDialog(CreateGroupActivity.this, R.style.AlertDialog);
            mProgressDialog.setTitle("Creating group...");
            mProgressDialog.setMessage("Please wait while we creating a new group.");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            //also add current user, as he is also a group member
            selectedFriendIdList.add(mCurrent_user_id);
            String members = "";
            for (String id : selectedFriendIdList) {
                members = members + (id.trim() + "///");
            }

            HashMap<String, String> userMap = new HashMap<>();
            userMap.put("id", group_id);
            userMap.put("name", group_name);
            userMap.put("admin", mCurrent_user_id);
            userMap.put("creation_time", String.valueOf(System.currentTimeMillis()));
            userMap.put("image", "default");
            userMap.put("thumb_image", "default");
            userMap.put("info", group_info);
            userMap.put("members", members);

            groupDatabase.child(group_id).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // add group id into chat to all the selected group members
                        for (String id : selectedFriendIdList) {
                            HashMap group = new HashMap();
                            group.put("seen", false);
                            group.put("timestamp", System.currentTimeMillis());
                            chatDatabase.child(id).child(group_id).updateChildren(group, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        uploadImage();
//                                        Toast.makeText(getApplicationContext(), "group creation successful ", Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                        startActivity(intent);
                                    } else {
                                        //delete group from group
                                        Toast.makeText(getApplicationContext(), "group creation failed ", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Failed to add group id into chat" + databaseError.getMessage());
                                        mProgressDialog.dismiss();
                                        //create_group.setEnabled(true);
                                    }
                                }
                            });
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "group creation failed ", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Failed to add group id into Groups");
                        mProgressDialog.dismiss();
                        //create_group.setEnabled(true);
                    }
                }
            });
        }else {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage() {
        mProgressDialog.setTitle("Uploading Image...");
        mProgressDialog.setMessage("Please wait while we upload and process the image.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] thumb_byte = baos.toByteArray();

        StorageReference filepath = mImageStorage.child("group_images").child(group_id + ".jpg");
        final StorageReference thumb_filepath = mImageStorage.child("group_images").child("thumbs").child(group_id + ".jpg");

        filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String download_url = task.getResult().getDownloadUrl().toString();
                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                            String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                            if (thumb_task.isSuccessful()) {
                                Map update_hashMap = new HashMap();
                                update_hashMap.put("image", download_url);
                                update_hashMap.put("thumb_image", thumb_downloadUrl);

                                groupDatabase.child(group_id).updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "group creation successful ", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);

                                            //Toast.makeText(CreateGroupActivity.this, "Successfully Uploaded.", Toast.LENGTH_LONG).show();
                                        }else {
                                            //delete group
                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(CreateGroupActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();
                            }
                        }
                    });
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Error in uploading.", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                }
            }
        });
    }
}
