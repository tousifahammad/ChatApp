package com.bibhas.handsapp.Activities;

import android.app.ProgressDialog;

import android.content.Intent;
import android.support.annotation.NonNull;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bibhas.handsapp.MainActivity;
import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Util.SharedMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class StartProfileActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;


    private Toolbar mtoolbar;
    private FirebaseAuth mAuth;
    private Button Next;

    private TextInputEditText mDisplayName;
    private TextInputEditText mDisplayStatus;

    private ProgressDialog mRegProgress;
    private DatabaseReference mDatabase;

    private ImageButton mIBtn;


    private StorageReference mImageStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_profile);
        mAuth = FirebaseAuth.getInstance();

        mtoolbar = findViewById(R.id.sprofile_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Setting Profile");

        //mRegProgress = new ProgressDialog(this, R.style.AlertDialog);
        mRegProgress = SharedMethods.showProgressDialog(this,"Please wait", "Updation info");
        mRegProgress.show();

        Next = findViewById(R.id.btn_next);
        mDisplayName = findViewById(R.id.display_name);
        mDisplayStatus = findViewById(R.id.display_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    mDisplayName.setText(name);
                    mDisplayStatus.setText(status);
                    mRegProgress.hide();
                }else {
                    mRegProgress.hide();
                    Toast.makeText(StartProfileActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mRegProgress.hide();
            }
        });


        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addtofirebase();
            }
        });
    }

    private void addtofirebase() {
        String display_name = mDisplayName.getText().toString().trim();
        String display_status = mDisplayStatus.getText().toString().trim();

        String phonenumber = getIntent().getStringExtra("phonenumber");

        if ((!TextUtils.isEmpty(display_name)) && (!TextUtils.isEmpty(display_status))) {
            mRegProgress.show();
            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = current_user.getUid();

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

            String device_token = FirebaseInstanceId.getInstance().getToken();

            HashMap<String, String> userMap = new HashMap<>();
            userMap.put("name", display_name);
            userMap.put("status", display_status);
            userMap.put("mobile_no", phonenumber);
            userMap.put("image", "default");
            userMap.put("thumb_image", "default");
            userMap.put("device_token", device_token);

            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent mainIntent = new Intent(StartProfileActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                        mRegProgress.dismiss();
                    }else {
                        mRegProgress.hide();
                    }
                }
            });
        } else {
            Toast.makeText(StartProfileActivity.this, "Kindly Enter all the fields", Toast.LENGTH_SHORT).show();
        }
    }


}




