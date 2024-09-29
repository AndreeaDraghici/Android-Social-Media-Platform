package com.ucv.ace.socialmediaplatform.service.post.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelUsers;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The {@code AdapterUsers} is a RecyclerView Adapter that binds user data
 * to a list item layout for display in a RecyclerView.
 * It handles user images, names, and emails, allowing for easy visualization
 * of a list of users within the social media platform.
 */
public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    FirebaseAuth firebaseAuth;
    String uid;

    /**
     * Constructs a new AdapterUsers instance.
     *
     * @param context The context in which the adapter is operating.
     * @param list    A list of ModelUsers representing the users to display.
     */
    public AdapterUsers(Context context, List<ModelUsers> list) {
        this.context = context;
        this.list = list;
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getUid();
    }

    List<ModelUsers> list;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    /**
     * Binds the user data to the views in the specified position of the RecyclerView.
     *
     * @param holder   The ViewHolder that will hold the views.
     * @param position The position of the item in the data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        final String hisuid = list.get(position).getUid();
        String userImage = list.get(position).getImage();
        String username = list.get(position).getName();
        String usermail = list.get(position).getEmail();
        holder.name.setText(username);
        holder.email.setText(usermail);
        try {
            Glide.with(context).load(userImage).into(holder.profiletv);
        } catch (Exception e) {
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The number of items in the list.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder class to hold the views for each user item.
     */
    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profiletv;
        TextView name, email;

        /**
         * Constructs a new MyHolder instance and initializes the views.
         *
         * @param itemView The view for this holder.
         */
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = itemView.findViewById(R.id.imagep);
            name = itemView.findViewById(R.id.namep);
            email = itemView.findViewById(R.id.emailp);
        }
    }
}