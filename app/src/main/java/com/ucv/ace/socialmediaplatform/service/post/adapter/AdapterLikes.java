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

import java.util.ArrayList;
import java.util.List;

public class AdapterLikes extends RecyclerView.Adapter<AdapterLikes.Holder> {
    private List<ModelUsers> users = new ArrayList<>();

    public void setUsers(List<ModelUsers> u) {
        this.users = u;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_like, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        ModelUsers u = users.get(i);
        h.tvName.setText(u.getName());
        // load profile photo
        Glide.with(h.img.getContext())
                .load(u.getImage())
                .placeholder(R.drawable.ic_image)
                .into(h.img);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView img;

        Holder(View iv) {
            super(iv);
            img = iv.findViewById(R.id.img_avatar);
            tvName = iv.findViewById(R.id.tv_username);
        }
    }
}
