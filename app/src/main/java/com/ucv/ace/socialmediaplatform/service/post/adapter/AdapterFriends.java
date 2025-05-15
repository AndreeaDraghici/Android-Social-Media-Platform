package com.ucv.ace.socialmediaplatform.service.post.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelUsers;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class AdapterFriends extends RecyclerView.Adapter<AdapterFriends.FriendsViewHolder>{

    public interface OnFriendClickListener {
        void onFriendClick(ModelUsers user);
    }

    private List<ModelUsers> friends;
    private OnFriendClickListener listener;

    public AdapterFriends(List<ModelUsers> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        ModelUsers user = friends.get(position);
        holder.name.setText(user.getName());
        Glide.with(holder.itemView.getContext()).load(user.getImage()).into(holder.avatar);
        holder.itemView.setOnClickListener(v -> listener.onFriendClick(user));

    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView avatar;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.friendName);
            avatar = itemView.findViewById(R.id.friendAvatar);
        }
    }
}
