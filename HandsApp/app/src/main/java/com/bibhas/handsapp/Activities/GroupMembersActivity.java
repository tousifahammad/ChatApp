package com.bibhas.handsapp.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bibhas.handsapp.Fragments.FriendsFragment;
import com.bibhas.handsapp.FriendListFragment;
import com.bibhas.handsapp.MainActivity;
import com.bibhas.handsapp.Models.Users;
import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Util.SharedData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMembersActivity extends AppCompatActivity {
    private static final String TAG = "GroupMembersActivity==";
    private RecyclerView mFriendsList;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference groupDatabase;
    private DatabaseReference chatDatabase;
    private ProgressBar ga_progress_bar;
    private String group_id, name, info, admin_id, members, image_url;
    private RelativeLayout rl_friends;
    private FriendsFragment friendFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    public static ArrayList<String> selectedMembersList = new ArrayList<>();
    public static TextView gma_selected_members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        ga_progress_bar = findViewById(R.id.ga_progress_bar);
        mFriendsList = findViewById(R.id.friends_list);
        rl_friends = findViewById(R.id.ll_friends);
        gma_selected_members = findViewById(R.id.gma_selected_members);
        rl_friends.setVisibility(View.GONE);

        group_id = getIntent().getStringExtra("group_id");
        admin_id = getIntent().getStringExtra("group_admin");
        //members = getIntent().getStringExtra("group_members");

        //mCurrent_user_id = mAuth.getCurrentUser().getUid();

        //mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        //mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        groupDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
        chatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");

        setUpAppBar();

        getGroupDetails();

        //setGroupDetails();

        //mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        friendFragment = new FriendsFragment();
        fragmentManager = this.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.commit();
    }

    private void setUpAppBar() {
        Toolbar mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle(currentGroupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();

        } else if (id == R.id.leave_group) {
            showWarning();

        } else if (id == R.id.add_member) {
            addMember();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showWarning() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Confirmation!!!")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        leaveGroup();
                    }
                })
                .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .show();
    }


    @Override
    public void onStart() {
        super.onStart();

        ga_progress_bar.setVisibility(View.VISIBLE);

        FirebaseRecyclerAdapter<Users, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Users, FriendsViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Users users, int i) {
                //friendsViewHolder.setDate(users.getDate());
                final String list_user_id = getRef(i).getKey();

                if (members.contains(list_user_id)) {
                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            final String userStatus = dataSnapshot.child("status").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            friendsViewHolder.setName(userName);
                            friendsViewHolder.setStatus(userStatus);
                            friendsViewHolder.setUserImage(userThumb, getApplicationContext());
                            if (dataSnapshot.hasChild("online")) {
                                String userOnline = String.valueOf(dataSnapshot.child("online").getValue());
                                friendsViewHolder.setUserOnline(userOnline);
                            }

                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //Toast.makeText(GroupMembersActivity.this, userName, Toast.LENGTH_SHORT).show();
                                    PopupMenu popup = new PopupMenu(GroupMembersActivity.this, v);
                                    popup.getMenuInflater().inflate(R.menu.ex_popup_menu, popup.getMenu());

                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        public boolean onMenuItemClick(MenuItem item) {
                                            if (admin_id.equals(SharedData.current_user_id)) {
                                                if (item.getItemId() == R.id.add) {
                                                    changeGroupAdmin(list_user_id);
                                                } else if (item.getItemId() == R.id.rem) {
                                                    removeGroupMember(list_user_id);
                                                }
                                            } else {
                                                Toast.makeText(GroupMembersActivity.this, "Only admin can do this", Toast.LENGTH_SHORT).show();
                                            }
                                            return true;
                                        }
                                    });
                                    popup.show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                } else {
                    friendsViewHolder.mView.setVisibility(View.GONE);
                    friendsViewHolder.mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
        ga_progress_bar.setVisibility(View.GONE);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumb_image, Context ctx) {
            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }

        public void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.user_single_online_icon);
            if (online_status.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void getGroupDetails() {
        try {
            groupDatabase.child(group_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("name").getValue().toString();
                    info = dataSnapshot.child("info").getValue().toString();
                    admin_id = dataSnapshot.child("admin").getValue().toString();
                    members = dataSnapshot.child("members").getValue().toString();
                    image_url = dataSnapshot.child("image").getValue().toString();

                    setGroupDetails();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            getSupportActionBar().setTitle("Group");
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }

    private void setGroupDetails() {
        try {
            TextView group_info = findViewById(R.id.group_info);
            final ImageView group_thumb_image = findViewById(R.id.group_image);

            getSupportActionBar().setTitle(name);
            group_info.setText(info);
            setAdminName();
            Picasso.with(getApplicationContext()).load(image_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(group_thumb_image, new Callback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onError() {
                    Picasso.with(getApplicationContext()).load(image_url).placeholder(R.drawable.default_avatar).into(group_thumb_image);
                }
            });
            //reload activity
            onStart();

        } catch (Exception e) {
            getSupportActionBar().setTitle("Group");
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }

    private void setAdminName() {
        final TextView group_admin = findViewById(R.id.admin);
        //get admin name from admin id
        mUsersDatabase.child(admin_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String admin = "Admin : " + dataSnapshot.child("name").getValue().toString();
                group_admin.setText(admin);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * delete member id from group
     * make first member as admin if user is admin
     * delete group from chat
     * delete group if no other member a present
     */
    private void leaveGroup() {
        try {
            // remove current user from group member
            members = members.replace(SharedData.current_user_id + "///", "");
            updateGroupMembers();

            //make first member as admin if the removed user is admin
            String group_members[] = members.split("///");
            if (group_members.length > 0) {
                if (admin_id.equals(SharedData.current_user_id)) {
                    String first_member_id = group_members[0];
                    groupDatabase.child(group_id).child("admin").setValue(first_member_id);
                }
            } else {
                groupDatabase.child(group_id).removeValue();
            }

            // remove from chat group id
            chatDatabase.child(SharedData.current_user_id).child(group_id).removeValue();

            Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }

    private void changeGroupAdmin(String id) {
        try {
            if (admin_id.equals(SharedData.current_user_id)) {
                if (admin_id.equals(id)) {
                    Toast.makeText(getApplicationContext(), "You are already the group admin", Toast.LENGTH_SHORT).show();
                } else {
                    groupDatabase.child(group_id).child("admin").setValue(id);
                    admin_id = id;
                    setAdminName();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Only admin can do this", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }

    private void removeGroupMember(String id) {
        try {
            if (!id.equals(SharedData.current_user_id)) {
                members = members.replace(id + "///", "");
                updateGroupMembers();
                //reload activity
                onStart();
            } else {
                Toast.makeText(getApplicationContext(), "You can't remove yourself", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }
    }


    private void addMember() {
        rl_friends.setVisibility(View.VISIBLE);
        selectedMembersList.clear();
        fragmentTransaction.replace(R.id.fragment_friend_list, friendFragment);
    }

    public void later(View view) {
        detachFragment();
    }

    public void addMember(View view) {
        view.setEnabled(false);
        for (String id : selectedMembersList) {
            if (members.contains(id)) {
                Toast.makeText(this, "Some selected member also present in this group", Toast.LENGTH_SHORT).show();
            } else {
                members = members + id + "///";
            }
        }
        updateGroupMembers();
        view.setEnabled(true);
        detachFragment();

        //reload activity
        onStart();
    }

    private void detachFragment() {
        fragmentTransaction.remove(friendFragment);
        rl_friends.setVisibility(View.GONE);
    }

    private void updateGroupMembers() {
        groupDatabase.child(group_id).child("members").setValue(members);
    }
}
