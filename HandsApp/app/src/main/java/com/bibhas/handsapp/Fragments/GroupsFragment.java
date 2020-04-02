package com.bibhas.handsapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bibhas.handsapp.Adapters.GroupsAdapter;
import com.bibhas.handsapp.Activities.GroupMembersActivity;
import com.bibhas.handsapp.Models.Group;
import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Util.SharedData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link GroupsFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link GroupsFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class GroupsFragment extends Fragment implements GroupsAdapter.ClickListener {
    private static final String TAG = "GroupsFragment";
    //private ArrayList<Group> groupList = new ArrayList<>();
    private View groupFragmentView;
    //private ListView list_view;
    //private ArrayAdapter<String> arrayAdapter;
    private ArrayList<Group> list_of_groups = new ArrayList<>();
    private DatabaseReference GroupRef;
    private DatabaseReference chatRef;
    private RecyclerView recyclerView;
    //private ProgressBar progressBar;
    private GroupsAdapter groupsAdapter;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        //String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat");
        chatRef.keepSynced(true);
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupRef.keepSynced(true);

        setRecyclerView();

        RetrieveAndDisplayGroup();

        return groupFragmentView;

    }

    private void setRecyclerView() {
        recyclerView = groupFragmentView.findViewById(R.id.groups);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        groupsAdapter = new GroupsAdapter(list_of_groups);
        groupsAdapter.setClickListener(this);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(groupsAdapter);
    }

    private void RetrieveAndDisplayGroup() {
        //Log.d(TAG, "RetrieveAndDisplayGroup: " + SharedData.current_user_id);
        chatRef.child(SharedData.current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        final String group_id = child.getKey().trim();
                        if (group_id.startsWith(SharedData.group_id_prefix)) {

                            GroupRef.child(group_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String info = dataSnapshot.child("info").getValue().toString();
                                    String members = dataSnapshot.child("members").getValue().toString();
                                    String admin = dataSnapshot.child("admin").getValue().toString();
                                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                                    String image = dataSnapshot.child("image").getValue().toString();

                                    Group group = new Group();
                                    group.setId(group_id);
                                    group.setName(name);
                                    group.setInfo(info);
                                    group.setMembers(members);
                                    group.setAdmin(admin);
                                    group.setThumb_image(thumb_image);
                                    group.setFull_image(image);

                                    boolean group_present = false;
                                    for (Group group1 : list_of_groups) {
                                        if (group1.getId().equals(group_id)) {
                                            group_present = true;
                                            break;
                                        }
                                    }
                                    if (!group_present) {
                                        list_of_groups.add(group);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                    Log.d(TAG, "list_of_groups: " + list_of_groups.toString());
                    groupsAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

//        GroupRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                list_of_groups.clear();
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    HashMap<String, String> hashMap = SharedMethods.stringToHashMap(child.getValue().toString());
//                    Group group = new Group();
//
//                    group.setId(hashMap.get("id"));
//                    group.setName(hashMap.get("name"));
//                    group.setThumb_image(hashMap.get("thumb_image"));
//                    group.setInfo(hashMap.get("info"));
//
//                    list_of_groups.add(group);
//                }
//                groupsAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) { }
//        });
    }

    @Override
    public void itemClicked(View view, int position) {
        Intent groupIntent = new Intent(getContext(), GroupMembersActivity.class);
        groupIntent.putExtra("group_id", list_of_groups.get(position).getId());
        groupIntent.putExtra("group_name", list_of_groups.get(position).getName());
        groupIntent.putExtra("group_info", list_of_groups.get(position).getInfo());
        groupIntent.putExtra("group_admin", list_of_groups.get(position).getAdmin());
        groupIntent.putExtra("group_members", list_of_groups.get(position).getMembers());
        groupIntent.putExtra("group_thumb_image", list_of_groups.get(position).getThumb_image());
        groupIntent.putExtra("group_image", list_of_groups.get(position).getFull_image());
        startActivity(groupIntent);
    }
}