package com.bibhas.handsapp.Activities;


import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bibhas.handsapp.CallScreenActivity;
import com.bibhas.handsapp.Util.GetTimeAgo;
import com.bibhas.handsapp.MainActivity;
import com.bibhas.handsapp.Adapters.MessageAdapter;
import com.bibhas.handsapp.Models.Messages;
import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Services.BaseActivity;
import com.bibhas.handsapp.Services.SinchService;
import com.bibhas.handsapp.Util.SharedData;
import com.bibhas.handsapp.Util.SharedMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.calling.Call;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

import static com.bibhas.handsapp.R.*;
import static com.bibhas.handsapp.R.drawable.*;


public class ChatActivity extends BaseActivity implements View.OnClickListener {
    // mCurrentUser-sender, mChatUser - receiver
    private final static String TAG = "ChatActivity==";
    private LinearLayout mRevealView;
    private boolean hidden = true;
    private ImageButton gallery_btn, photo_btn, video_btn, audio_btn, location_btn, contact_btn, camera_btn;
    private String mChatUser;
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserId;
    private ImageButton mChatAddBtn, photo_img_btn;
    private FloatingActionButton mChatSendBtn;
    private EditText mChatMessageView;

    //private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 1000;
    private int mCurrentPage = 1;
    private static final int GALLERY_PICK = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int AUDIO_PICK = 3;
    private static final int VIDEO_PICK = 4;
    private static final int PICKFILE_REQUEST_CODE = 5;
    // Storage Firebase
    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;
    private ProgressBar progressBar;
    //New Solution
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private Uri mImageUri = null;
    private Handler mHandler;
    private String userName;
    private String userProfileImageUrl;
    //group
    private String chat_type;
    private ArrayList<String> chat_receiver_list = new ArrayList<>();
    private DatabaseReference messageRef;
    //notification
    public static String notification_message = "Got new message";

    public static boolean isFriend;
    public static boolean isGroupMember;
    public static String chattingWithTheUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_chat);

        initView();
        this.mHandler = new Handler();
        m_Runnable.run();

        mChatToolbar = findViewById(id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mRootRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        // get data from intent
        try {
            if (getIntent().getBooleanExtra("is_single_chat", false)) {
                chat_type = SharedData.single_chat;
                mChatUser = getIntent().getStringExtra("user_id");
                userName = getIntent().getStringExtra("user_name");
                chat_receiver_list.add(mCurrentUserId);
                chat_receiver_list.add(mChatUser);
                checkFriendShip();
            } else {
                chat_type = SharedData.group_chat;
                mChatUser = getIntent().getStringExtra("group_id");
                userName = getIntent().getStringExtra("group_name");
                getChatReceiverList(mChatUser);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

//        // === requestPermission ======
//        requestPermission();

        // ---- Custom Action bar Items ----
        mTitleView = findViewById(id.custom_bar_title);
        mLastSeenView = findViewById(id.custom_bar_seen);
        mProfileImage = findViewById(id.custom_bar_image);

        mChatAddBtn = findViewById(id.chat_add_btn);
        mChatSendBtn = findViewById(id.floatingButton);
        mChatMessageView = findViewById(id.chat_message_view);

        progressBar = findViewById(id.progress_bar);
        progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        camera_btn = findViewById(id.btn_cam);

        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cam_pick();
            }
        });


        // ======== set recycler view ==============
        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = findViewById(id.messages_list);
//        mRefreshLayout = (SwipeRefreshLayout) findViewById(id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mLinearLayout.setStackFromEnd(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        mTitleView.setText(userName);
        chattingWithTheUserName = userName;

//        if (chat_type.equals("single_chat")) {
//            checkFriendShip();
//            if (isFriend) {
//                setSingleUserDetails();
//            } else {
//                mChatSendBtn.setEnabled(false);
//                Toast.makeText(this, "You are no longer friends with this person", Toast.LENGTH_LONG).show();
//            }
//        } else if (chat_type.equals("group_chat")) {
//
//            Log.d(TAG, "member_list2: " + chat_receiver_list.toString());
//            Log.d(TAG, "mChatUser2: " + mChatUser);
//            Log.d(TAG, "mCurrentUserId2: " + mCurrentUserId);
//            Log.d(TAG, "isGroupMember2: " + isGroupMember);
//
//            if (isGroupMember) {
//                setGroupDetails() ;
//            } else {
//                mChatSendBtn.setEnabled(false);
//                Toast.makeText(this, "You are no longer member of this group", Toast.LENGTH_LONG).show();
//            }
//        }
//        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                userProfileImageUrl = "";
//                String online = dataSnapshot.child("online").getValue().toString();
//                userProfileImageUrl = dataSnapshot.child("image").getValue().toString();
//
//                if (!userProfileImageUrl.equals("default")) {
//
//                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
//
//                    Picasso.with(ChatActivity.this).load(userProfileImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
//                            .placeholder(default_avatar).into(mProfileImage, new Callback() {
//                        @Override
//                        public void onSuccess() {
//
//                        }
//
//                        @Override
//                        public void onError() {
//
//                            Picasso.with(ChatActivity.this).load(userProfileImageUrl).placeholder(default_avatar).into(mProfileImage);
//
//                        }
//                    });
//
//                }
//
//                if (online.equals("true")) {
//
//                    mLastSeenView.setText("online");
//
//                } else {
//
//                    GetTimeAgo getTimeAgo = new GetTimeAgo();
//
//                    long lastTime = Long.parseLong(online);
//
//                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
//
//                    mLastSeenView.setText(lastSeenTime);
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


//        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                if (!dataSnapshot.hasChild(mChatUser)) {
//
//                    Map chatAddMap = new HashMap();
//                    chatAddMap.put("seen", false);
//                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
//
//                    Map chatUserMap = new HashMap();
//                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
//                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);
//
//                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//                            if (databaseError != null) {
//
//                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
//
//                            }
//                        }
//                    });
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cx = (mRevealView.getLeft() + mRevealView.getRight());
                int cy = mRevealView.getTop();
                int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

                //Below Android LOLIPOP Version
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    SupportAnimator animator =
                            ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(700);

                    SupportAnimator animator_reverse = animator.reverse();

                    if (hidden) {
                        mRevealView.setVisibility(View.VISIBLE);
                        animator.start();
                        hidden = false;
                    } else {
                        animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                            @Override
                            public void onAnimationStart() {
                            }

                            @Override
                            public void onAnimationEnd() {
                                mRevealView.setVisibility(View.INVISIBLE);
                                hidden = true;
                            }

                            @Override
                            public void onAnimationCancel() {
                            }

                            @Override
                            public void onAnimationRepeat() {
                            }
                        });
                        animator_reverse.start();
                    }

                } else {     // Android LOLIPOP And ABOVE Version
                    if (hidden) {
                        Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                        mRevealView.setVisibility(View.VISIBLE);
                        anim.start();
                        hidden = false;
                    } else {
                        Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, radius, 0);
                        mRevealView.setVisibility(View.INVISIBLE);
                        hidden = true;
                        anim.start();
                    }
                }
            }
        });
    }

    private void checkFriendShip() {
        mRootRef.child("Friends").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.hasChild(mChatUser)) {
                        isFriend = true;
                        setSingleUserDetails();
                        loadMessages();
                    } else {
                        Toast.makeText(ChatActivity.this, "You are no longer friends with this person", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                    isFriend = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                isFriend = false;
            }
        });
    }

    public void getChatReceiverList(String group_id) {
        mRootRef.child("Groups").child(group_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String members = dataSnapshot.child("members").getValue().toString();
                String member_list[] = members.trim().split("///");

                for (String id : member_list) {
                    chat_receiver_list.add(id);
                    // check if Current User is GroupMember
                    if (id.equals(mCurrentUserId)) {
                        isGroupMember = true;
                    }
                }
                if (isGroupMember) {
                    setGroupDetails();
                    loadMessages();
                } else {
                    Toast.makeText(ChatActivity.this, "You are no longer member of this group", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //set Single User Details in the app bar
    private void setSingleUserDetails() {
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userProfileImageUrl = "";
                String online = dataSnapshot.child("online").getValue().toString();
                userProfileImageUrl = dataSnapshot.child("image").getValue().toString();

                if (!userProfileImageUrl.equals("default")) {
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                    Picasso.with(ChatActivity.this).load(userProfileImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(default_avatar).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(ChatActivity.this).load(userProfileImageUrl).placeholder(default_avatar).into(mProfileImage);
                        }
                    });
                }

                if (online.equals("true")) {
                    mLastSeenView.setText("online");

                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //============================================
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //============================================
        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
    }

    //set group Details in the app bar
    private void setGroupDetails() {
        mRootRef.child("Groups").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String info = dataSnapshot.child("info").getValue().toString();
                if (!info.equals(null)) {
                    mLastSeenView.setSingleLine(true);
                    mLastSeenView.setWidth(500);
                    mLastSeenView.setText(info);
                }

                userProfileImageUrl = dataSnapshot.child("image").getValue().toString();
                if (!userProfileImageUrl.equals("default")) {
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                    Picasso.with(ChatActivity.this).load(userProfileImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(default_avatar).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(ChatActivity.this).load(userProfileImageUrl).placeholder(default_avatar).into(mProfileImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        chattingWithTheUserName = userName;
    }

    @Override
    protected void onStop() {
        super.onStop();
        chattingWithTheUserName = null;
    }

    private final Runnable m_Runnable = new Runnable() {
        public void run() {
            ChatActivity.this.mHandler.postDelayed(m_Runnable, 1000);
        }
    };

    private void initView() {

        mRevealView = findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.GONE);

        gallery_btn = findViewById(R.id.gallery_img_btn);
        photo_btn = findViewById(R.id.photo_img_btn);
        video_btn = findViewById(R.id.video_img_btn);
        audio_btn = findViewById(R.id.audio_img_btn);
        location_btn = findViewById(id.file_btn);
        contact_btn = findViewById(R.id.contact_img_btn);

        photo_img_btn = findViewById(id.photo_img_btn);

        gallery_btn.setOnClickListener(this);
        photo_btn.setOnClickListener(this);
        video_btn.setOnClickListener(this);
        audio_btn.setOnClickListener(this);
        location_btn.setOnClickListener(this);
        contact_btn.setOnClickListener(this);
    }


    private void loadMessages() {
        if (!isFriend && !isGroupMember) {
            Toast.makeText(ChatActivity.this, "Please check the reason", Toast.LENGTH_LONG).show();
            Log.d(TAG, "isFriend: " + isFriend);
            Log.d(TAG, "isFriend: " + isGroupMember);
            return;
        }
        messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByChild("time").limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Log.d(TAG, "onChildAdded: "+ dataSnapshot.getChildrenCount());
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos == 1) {
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(mAdapter.getItemCount() - 1);
//                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * for single chat message send to single receiver
     * for group chat message send to all group members
     * for sender
     * here mChatUser is group id for group
     * String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
     * for group 'sender id' will be 'group id'
     * update all group members messages
     */

    private void sendMessage() {
        if (!isFriend && !isGroupMember) {
            Toast.makeText(ChatActivity.this, "Please check the reason", Toast.LENGTH_LONG).show();
            Log.d(TAG, "isFriend: " + isFriend);
            Log.d(TAG, "isFriend: " + isGroupMember);
            return;
        }

        final String message = mChatMessageView.getText().toString();
        mChatMessageView.setText("");
        String file_id = SharedData.text_file_type + SharedMethods.randomAlphaNumeric();
        if (!TextUtils.isEmpty(message)) {
            notification_message = message;

            for (String receiver_id : chat_receiver_list) {
                if (chat_type.equals(SharedData.single_chat)) {
                    if (receiver_id.equals(mCurrentUserId)) {
                        updateMessage(receiver_id, mChatUser, message, file_id, SharedData.text_file_type, "default");
                        updateChat(mCurrentUserId, mChatUser);
                    } else {
                        updateMessage(receiver_id, mCurrentUserId, message, file_id, SharedData.text_file_type, "default");
                        updateChat(receiver_id, mCurrentUserId);
                    }
                } else {
                    updateMessage(receiver_id, mChatUser, message, file_id, SharedData.text_file_type, "default");
                    updateChat(receiver_id, mChatUser);
                }
            }
        }
    }

    private void updateChat(String sender_id, String receiver_id) {
        mRootRef.child("Chat").child(sender_id).child(receiver_id).child("seen").setValue(false);
        mRootRef.child("Chat").child(sender_id).child(receiver_id).child("timestamp").setValue(ServerValue.TIMESTAMP);
    }

    private void updateMessage(String sender_id, String receiver_id, final String message, String message_id, String file_type, String thumb_image) {
        String chat_user_ref;
        mRootRef.child("messages").child(sender_id).child(receiver_id).push();
        chat_user_ref = "messages/" + sender_id + "/" + receiver_id;
        //String push_id = user_message_push.getKey();

        Map messageMap = new HashMap();
        messageMap.put("message", message);
        messageMap.put("seen", false);
        messageMap.put("type", file_type);
        messageMap.put("time", ServerValue.TIMESTAMP);
        messageMap.put("from", mCurrentUserId);
        messageMap.put("thumb_image", thumb_image);

        Map messageGroupUserMap = new HashMap();

        messageGroupUserMap.put(chat_user_ref + "/" + message_id, messageMap);

        final String temp_id = sender_id;
        mRootRef.updateChildren(messageGroupUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d("CHAT_LOG", databaseError.getMessage());
                } else {
                    if (!temp_id.equals(mCurrentUserId)) {
                        SharedMethods.sendPushNotification(temp_id, notification_message, mCurrentUserId);
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.context_menu, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        hideRevealView();
        switch (v.getId()) {

            case R.id.gallery_img_btn:
                gall_pick();
                break;

            case R.id.photo_img_btn:
                cam_pick();
                break;

            case R.id.video_img_btn:
                video_pick();
                break;

            case R.id.audio_img_btn:
                audio_pick();
                break;

            case R.id.file_btn:
                file_pick();
                break;

            case R.id.contact_img_btn:
                Toast.makeText(this, "Select Contact", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void cam_pick() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }

    private void gall_pick() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        Toast.makeText(ChatActivity.this, "Please select an Image File", Toast.LENGTH_SHORT).show();
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
    }

    private void audio_pick() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Audio "), AUDIO_PICK);
    }

    private void video_pick() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video "), VIDEO_PICK);
    }


    private void file_pick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    private void hideRevealView() {
        if (mRevealView.getVisibility() == View.VISIBLE) {
            mRevealView.setVisibility(View.GONE);
            hidden = true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data != null) {
                Uri uri = data.getData();

                if (uri != null) {      // uri is null for camera image
                    if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
                        sendFileToFirebase(SharedData.image_file_type, uri);

                    } else if (requestCode == AUDIO_PICK && resultCode == RESULT_OK) {
                        sendFileToFirebase(SharedData.audio_file_type, uri);

                    } else if (requestCode == VIDEO_PICK && resultCode == RESULT_OK) {
                        sendFileToFirebase(SharedData.video_file_type, uri);

                    } else if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK) {
                        sendFileToFirebase(SharedData.file_one_file_type, uri);
                    }
                } else {
                    if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        Uri img_uri = SharedMethods.getImageUri(getApplicationContext(), bitmap);
                        if (img_uri != null) {
                            sendFileToFirebase(SharedData.image_file_type, img_uri);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }

//    private void updateAdapter(String image_url,String type, boolean b, Map<String, String> timestamp, String mCurrentUserId) {
//        Messages messagesObj = new Messages();
//        messagesObj.setMessage(image_url);
//        messagesObj.setSeen(b);
//        messagesObj.setTime(1537248181);
//        messagesObj.setType(type);
//        messagesObj.setFrom(mCurrentUserId);
//
//        messagesList.add(messagesObj);
//
//        mAdapter.notifyItemInserted(messagesList.size());
//    }


    private void sendFileToFirebase(final String file_type, final Uri file_uri) {
        String file_name = "";
        String file_format = "";
        String storing_path = "";
        String thumb_image_storing_path = "";

        progressBar.setVisibility(View.VISIBLE);

        //final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
        //final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

        //DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

//        final String push_id = user_message_push.getKey();
        String file_id = SharedMethods.randomAlphaNumeric();
        switch (file_type) {
            case "image":
                file_format = SharedData.image_format;
                file_id = file_type + file_id;
                file_name = file_id + file_format;
                storing_path = SharedData.sent_image_dir;
                thumb_image_storing_path = SharedData.sent_thumb_image_dir;
                notification_message = "Sent a photo";
                break;

            case "audio":
                file_format = SharedData.audio_format;
                file_id = file_type + file_id;
                file_name = file_id + file_format;
                storing_path = SharedData.sent_audio_dir;
                notification_message = "Sent an audio";
                break;

            case "video":
                file_format = SharedData.video_format;
                file_id = file_type + file_id;
                file_name = file_id + file_format;
                storing_path = SharedData.sent_video_dir;
                thumb_image_storing_path = SharedData.sent_thumb_video_dir;
                notification_message = "Sent a video";
                break;

            case "file_one":
                file_format = SharedData.file_one_format;
                file_id = file_type + file_id;
                file_name = file_id + file_format;
                storing_path = SharedData.sent_file_one_dir;
                notification_message = "Sent a pdf file";
                break;

            default:
                break;
        }

        //store a copy of the file,that stored
        //storeFileCopyInDeviceStorage(file_uri, file_name);
        copyFile(file_uri, file_name, storing_path);

        final StorageReference filepath = mImageStorage.child("message_" + file_type + "/full_" + file_type).child(file_id + file_format);
        //EX: "message_image/full_images/imageLPa1aqddElIW-1dlvLd.jpg"
        //EX: "message_video/full_video/videoLPa1aqddElIW-1dlvLd.jpg"

        final String fil_message_id = file_id;
        final String thumb_storing_path = thumb_image_storing_path;
        final String thumb_file_name = "thumb" + file_id + SharedData.image_format;
        //EX: thumbimageLPa1aqddElIW-1dlvLd.jpg"

        //upload file to firebase storage
        filepath.putFile(file_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String full_image_url = task.getResult().getDownloadUrl().toString();

                    //upload thumb_image
                    if (file_type.equals("video") || file_type.equals("image")) {
                        Bitmap bitmap = null;
                        if (file_type.equals("image")) {
                            bitmap = SharedMethods.resizeImage(getApplicationContext(), file_uri, 100);
                        }

                        if (file_type.equals("video")) {
                            MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
                            mMMR.setDataSource(getApplicationContext(), file_uri);
                            bitmap = mMMR.getFrameAtTime();
                        }

                        if (bitmap != null) {
                            Uri thumb_image_uri = SharedMethods.getImageUri(getApplicationContext(), bitmap);

                            copyFile(thumb_image_uri, thumb_file_name, thumb_storing_path);

                            StorageReference filepath = mImageStorage.child("message_" + file_type + "/thumb_images").child(thumb_file_name);
                            filepath.putFile(thumb_image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setVisibility(View.GONE);
                                        //Toast.makeText(ChatActivity.this, file_type + " Sent Successfully", Toast.LENGTH_SHORT).show();
                                        String thumb_image_url = task.getResult().getDownloadUrl().toString();

                                        for (String receiver_id : chat_receiver_list) {
                                            if (chat_type.equals(SharedData.single_chat)) {
                                                if (receiver_id.equals(mCurrentUserId)) {
                                                    updateMessage(receiver_id, mChatUser, full_image_url, fil_message_id, file_type, thumb_image_url);
                                                } else {
                                                    updateMessage(receiver_id, mCurrentUserId, full_image_url, fil_message_id, file_type, thumb_image_url);
                                                }
                                            } else {
                                                updateMessage(receiver_id, mChatUser, full_image_url, fil_message_id, file_type, thumb_image_url);
                                            }
                                        }
                                    }
                                }
                            })
                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                            double x = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                            int progress = (int) Math.round(x);
                                            if (Build.VERSION.SDK_INT >= 24)
                                                progressBar.setProgress(progress, true);
                                            else
                                                progressBar.setProgress(progress);
                                        }
                                    })

                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(ChatActivity.this, "Sending " + file_type + "failed", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        } else {
                            Toast.makeText(ChatActivity.this, thumb_file_name + "upload failed", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: " + thumb_file_name + "upload failed");
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        //Toast.makeText(ChatActivity.this, file_type + " Sent Successfully", Toast.LENGTH_SHORT).show();

                        for (String receiver_id : chat_receiver_list) {
                            if (chat_type.equals(SharedData.single_chat)) {
                                if (receiver_id.equals(mCurrentUserId)) {
                                    updateMessage(receiver_id, mChatUser, full_image_url, fil_message_id, file_type, "default");
                                } else {
                                    updateMessage(receiver_id, mCurrentUserId, full_image_url, fil_message_id, file_type, "default");
                                }
                            } else {
                                updateMessage(receiver_id, mChatUser, full_image_url, fil_message_id, file_type, "default");
                            }
                        }
                    }
                }
            }
        })

                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double x = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        int progress = (int) Math.round(x);
                        if (Build.VERSION.SDK_INT >= 24)
                            progressBar.setProgress(progress, true);
                        else
                            progressBar.setProgress(progress);
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ChatActivity.this, "Sending " + file_type + "failed", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    private void copyFile(Uri file_uri, String file_name, String storing_path) {
        try {
            String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + storing_path;

            File myDir = new File(file_path);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            // get absolute path of that selected path
            String src = getFilePath(getApplicationContext(), file_uri);
            File source = new File(src);
            File destination = new File(myDir, file_name);
            // create a copy of that in the storage
            copy(source, destination);

        } catch (Exception e) {
            Log.d(TAG, "Exception 1: " + e.getMessage());
            Toast.makeText(this, "If can't use external storage, try to use other directory", Toast.LENGTH_LONG).show();
        }
    }


    private void copy(File source, File destination) {
        FileChannel in = null;
        FileChannel out = null;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(destination).getChannel();
            in.transferTo(0, in.size(), out);
        } catch (Exception e) {
            Log.d(TAG, "Exception:2 " + e.getMessage());
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (Exception e) {
                Log.d(TAG, "Exception: 3" + e.getMessage());
            }
        }
    }


    private String getFilePath(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {

            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                return Environment.getExternalStorageDirectory() + "/" + split[1];
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    Toast.makeText(context, "Could not get file path. Please try again", Toast.LENGTH_SHORT).show();
                }

            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            } else if (isMediaDocument(uri)) {

                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int item_id = item.getItemId();

        if (item_id == android.R.id.home) {
            onBackPressed();

        } else if (item_id == id.voice_call) {
            try {
                Call call = getSinchServiceInterface().callUser(mChatUser);
                callButtonClicked(call, mCurrentUserId, mChatUser, userName, userProfileImageUrl);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }

        } else if (item_id == id.video_call) {
            try {
                Call call = getSinchServiceInterface().callUserVideo(mChatUser);
                callButtonClicked(call, mCurrentUserId, mChatUser, userName, userProfileImageUrl);
            } catch (Exception e) {
                Log.d(TAG, "Exception: video_call " + e.getMessage());
            }

        } else if (item_id == id.delete_all) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, style.AlertDialog);
            builder.setTitle("Clear Conversation");
            builder.setMessage("Are you sure you want to Clear the Conversation?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

                    messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
//                            Log.e(TAG, "onCancelled", databaseError.toException());
                        }
                    });
                    Toast.makeText(ChatActivity.this, "Cleared", Toast.LENGTH_SHORT).show();
                    Intent settingsIntent = new Intent(ChatActivity.this, MainActivity.class);
                    startActivity(settingsIntent);
                    finish();
                }
            });

            builder.setNegativeButton("No", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else if (item.getItemId() == id.profile) {
            Intent settingsIntent = null;
            if (chat_type.equals(SharedData.group_chat)) {
                settingsIntent = new Intent(ChatActivity.this, GroupMembersActivity.class);
                settingsIntent.putExtra("group_id", mChatUser);
                settingsIntent.putExtra("group_name", "");
                settingsIntent.putExtra("group_info", "");
                settingsIntent.putExtra("group_admin", "");
                settingsIntent.putExtra("group_members", "");
                settingsIntent.putExtra("group_thumb_image", "");
                settingsIntent.putExtra("group_image", "");

            } else if (chat_type.equals(SharedData.single_chat)) {
                settingsIntent = new Intent(ChatActivity.this, ProfileActivity.class);
                settingsIntent.putExtra("user_id", mChatUser);
            }
            startActivity(settingsIntent);
        }
        return true;
    }


    // ============sinch================
    // invoked when the connection with SinchServer is established

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private void callButtonClicked(Call call, String callerId, String recipientId, String recipientName, String recipientImage) {
        try {
            if (call == null) {
                // Service failed for some reason, show a Toast and abort
                Toast.makeText(this, "Service is not started. Restarting the app.", Toast.LENGTH_LONG).show();
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return;
            }

            String callId = call.getCallId();
            Intent callScreen = new Intent(this, CallScreenActivity.class);

            callScreen.putExtra(SinchService.CALL_ID, callId);
            callScreen.putExtra("callerId", callerId);
            callScreen.putExtra("recipientId", recipientId);
            callScreen.putExtra("recipientName", recipientName);
            callScreen.putExtra("recipientImage", recipientImage);
            startActivity(callScreen);

        } catch (MissingPermissionException e) {
            ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
        }

    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now place a call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast.LENGTH_LONG).show();
        }
    }

}
