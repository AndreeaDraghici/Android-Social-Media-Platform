package com.ucv.ace.socialmediaplatform.service.post.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelUsers;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    private final Context context;
    private final List<ModelUsers> list;
    private final FirebaseAuth firebaseAuth;
    private final String uid;

    public AdapterUsers(Context context, List<ModelUsers> list) {
        this.context = context;
        this.list = list;
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ Ensure Correct Layout File is Used (item_user.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        ModelUsers user = list.get(position);
        String userImage = user.getImage();
        String username = user.getName();
        String userEmail = user.getEmail();

        // Prevent app crash by checking for null values
        holder.name.setText(username != null ? username : "Unknown User");
        holder.email.setText(userEmail != null ? userEmail : "No Email Provided");

        // Check if user image URL is empty or null before loading
        if (userImage == null || userImage.isEmpty()) {
            Glide.with(context)
                    .load(R.drawable.ic_image)  // Load a placeholder image
                    .into(holder.profileImage);
        } else {
            Glide.with(context)
                    .load(userImage)
                    .placeholder(R.drawable.ic_image) // Placeholder while loading
                    .error(R.drawable.ic_image) // If image fails to load
                    .into(holder.profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    /**
     * ViewHolder class to hold the views for each user item.
     */
    static class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView name, email;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.imagep);
            name = itemView.findViewById(R.id.namep);
            email = itemView.findViewById(R.id.emailp);
        }
    }
}
