package com.bibhas.handsapp.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bibhas.handsapp.Models.Users;
import com.bibhas.handsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FrindsListAdapter extends RecyclerView.Adapter<FrindsListAdapter.FrindsListViewHolder> {
    private ArrayList<Users> friendsList;
    private ClickListener clickListener;

    public FrindsListAdapter(ArrayList<Users> friendsList) {
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public FrindsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.users_single_layout, parent, false);
        return new FrindsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrindsListViewHolder holder, int position) {
        Users users = friendsList.get(position);
        String name = users.getName();
        String thumb_image_url = users.getThumb_image();
        String status = users.getStatus();

        holder.name.setText(name);
        Picasso.with(holder.name.getContext()).load(thumb_image_url).placeholder(R.drawable.default_avatar).into(holder.thumb_image);
        holder.status.setText(status);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class FrindsListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name;
        CircleImageView thumb_image;
        TextView status;

        public FrindsListViewHolder(@NonNull View itemView) {
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

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}