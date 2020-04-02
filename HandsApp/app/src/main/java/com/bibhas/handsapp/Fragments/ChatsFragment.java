package com.bibhas.handsapp.Fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bibhas.handsapp.Activities.ChatActivity;
import com.bibhas.handsapp.Activities.UsersActivity;
import com.bibhas.handsapp.Models.Conv;
import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Util.SharedData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {
    private static final String TAG = "ChatsFragment";
    private RecyclerView mConvList;
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mGroupsDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;
    private FloatingActionButton fab;
    private String chat_type;
    private ProgressBar chat_progress;


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);
        mConvList = mMainView.findViewById(R.id.conv_list);
        fab = mMainView.findViewById(R.id.fab);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);
        mGroupsDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
        mGroupsDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        chat_progress = mMainView.findViewById(R.id.chat_progress);
        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainView.findViewById(R.id.chat_progress).setVisibility(View.VISIBLE);
                Intent intent;
                intent = new Intent(getActivity(), UsersActivity.class);
                startActivity(intent);
            }
        });

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                Conv.class,
                R.layout.users_single_layout,
                ConvViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int i) {
                final String list_user_id = getRef(i).getKey();

                if (list_user_id.startsWith(SharedData.group_id_prefix)) {
                    chat_type = SharedData.group_chat;
                } else {
                    chat_type = SharedData.single_chat;
                }

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).orderByChild("time");
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        convViewHolder.setMessage(data);
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


                if (chat_type.equals(SharedData.single_chat)) {
                    // set image, name , online
                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                final String userName = dataSnapshot.child("name").getValue().toString();
                                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                                if (dataSnapshot.hasChild("online")) {
                                    String userOnline = dataSnapshot.child("online").getValue().toString();
                                    convViewHolder.setUserOnline(userOnline);
                                }

                                convViewHolder.setName(userName);
                                convViewHolder.setUserImage(userThumb, getContext());

                                //set button click
                                convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("user_id", list_user_id);
                                        chatIntent.putExtra("user_name", userName);
                                        chatIntent.putExtra("is_single_chat", true);
                                        startActivity(chatIntent);
                                    }
                                });
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    //set message seen or not
                    convViewHolder.setMessageSeen(conv.isSeen());

                } else if (chat_type.equals(SharedData.group_chat)) {
                    // set up for grop details
                    mGroupsDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String group_name = dataSnapshot.child("name").getValue().toString();
                            final String group_thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                            //Log.d(TAG, "group_name: " + group_name + group_thumb_image);
                            convViewHolder.setName(group_name);
                            convViewHolder.setUserImage(group_thumb_image, getContext());

                            //set button click
                            convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent groupChatIntent = new Intent(getContext(), ChatActivity.class);
                                    groupChatIntent.putExtra("group_id", list_user_id);
                                    groupChatIntent.putExtra("group_name", group_name);
                                    groupChatIntent.putExtra("is_single_chat", false);
                                    startActivity(groupChatIntent);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        };

        mConvList.setAdapter(firebaseConvAdapter);

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }


        public void setMessage(String message) {
            try {
                TextView userStatusView = mView.findViewById(R.id.user_single_status);
                if (message.contains(".jpg")) {
                    userStatusView.setText("Photo");

                } else if (message.contains(".mp3")) {
                    userStatusView.setText("Audio");

                } else if (message.contains(".mp4")) {
                    userStatusView.setText("Video");

                } else if (message.contains(".pdf")) {
                    userStatusView.setText("File");

                } else {
                    userStatusView.setText(message);
                }
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }

        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(final String thumb_image, final Context ctx) {
            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(userImageView, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
                }
            });

        }

        public void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.user_single_online_icon);
            if (online_status.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }

        public void setMessageSeen(boolean isSeen) {
            try {
                TextView userStatusView = mView.findViewById(R.id.user_single_status);
                if (!isSeen) {
                    userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD_ITALIC);
                } else {
                    userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
                }
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        chat_progress.setVisibility(View.GONE);
    }
}
