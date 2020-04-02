package com.bibhas.handsapp.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bibhas.handsapp.Models.Group;
import com.bibhas.handsapp.Models.Users;
import com.bibhas.handsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;
    private ArrayList<Group> groupsList;
    private GroupsAdapter.ClickListener clickListener;

    public GroupsAdapter(ArrayList<Group> groupsList) {
        this.groupsList = groupsList;
    }

    @NonNull
    @Override
    public GroupsAdapter.GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.users_single_layout, parent, false);
        return new GroupsAdapter.GroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupsAdapter.GroupsViewHolder holder, int position) {
        Group group = groupsList.get(position);
        String name = group.getName();
        String status = group.getInfo();
        final String thumb_image_url = group.getThumb_image();

        holder.name.setText(name);
        holder.status.setText(status);
        Picasso.with(holder.name.getContext()).load(thumb_image_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(holder.thumb_image, new Callback() {
            @Override
            public void onSuccess() {}
            @Override
            public void onError() {
                Picasso.with(holder.name.getContext()).load(thumb_image_url).placeholder(R.drawable.default_avatar).into(holder.thumb_image);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public class GroupsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name;
        CircleImageView thumb_image;
        TextView status;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_single_name);
            thumb_image = itemView.findViewById(R.id.user_single_image);
            status = itemView.findViewById(R.id.user_single_status);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.itemClicked(view, getLayoutPosition());
            }
        }
    }

    public interface ClickListener {
        public void itemClicked(View view, int position);
    }

    public void setClickListener(GroupsAdapter.ClickListener clickListener) {
        this.clickListener = clickListener;
    }

}
