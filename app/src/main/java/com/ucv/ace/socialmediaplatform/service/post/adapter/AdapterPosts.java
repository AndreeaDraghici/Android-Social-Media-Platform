package com.ucv.ace.socialmediaplatform.service.post.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelPost;
import com.ucv.ace.socialmediaplatform.service.activity.PostDetailsActivity;
import com.ucv.ace.socialmediaplatform.service.activity.PostLikedActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {
    private final Context context;
    private final List<ModelPost> posts;

    public AdapterPosts(Context context, List<ModelPost> posts) {
        this.context = context;
        this.posts   = posts;
    }

    @NonNull @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.row_posts, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder h, int i) {
        ModelPost post = posts.get(i);
        String pid     = post.getPid();
        String authorUid = post.getUid();

        // 1) Bind post data
        long ts = Long.parseLong(post.getPtime());
        h.timeTv.setText(DateFormat.format(
                "dd-MM-yyyy hh:mm aa",
                new Date(ts)
        ));
        h.titleTv.setText(post.getTitle());
        h.descriptionTv.setText(post.getDescription());
        Glide.with(context)
                .load(post.getUimage())
                .centerCrop()
                .placeholder(R.color.colorGray)
                .into(h.postImageIv);

        h.likeCountTv.setText(post.getPlike() + " Likes");
        h.commentCountTv.setText(post.getPcomments() + " Comments");

        // 2) Lookup author info from /Users/{authorUid}
        FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(authorUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        if (!snap.exists()) return;
                        String name   = snap.child("name" ).getValue(String.class);
                        String avatar = snap.child("image").getValue(String.class);
                        h.usernameTv.setText(name);
                        Glide.with(context)
                                .load(avatar)
                                .circleCrop()
                                .placeholder(R.drawable.ic_image)
                                .into(h.avatarIv);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { }
                });

        // 3) Likes count click → list likers
        h.likeCountTv.setOnClickListener(v -> {
            if (pid == null || pid.isEmpty()) {
                Toast.makeText(context, "Invalid post ID", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent likeIntent = new Intent(context, PostLikedActivity.class);
            likeIntent.putExtra("EXTRA_POST_ID", pid);
            context.startActivity(likeIntent);
        });

        // 4) Like button toggle
        h.likeBtn.setOnClickListener(v -> {
            FirebaseDatabase.getInstance()
                    .getReference("Posts")
                    .child(pid)
                    .child("Likes")
                    .child(FirebaseAuth.getInstance().getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        boolean already = task.getResult().exists();
                        if (already) {
                            // remove like
                            FirebaseDatabase.getInstance()
                                    .getReference("Posts")
                                    .child(pid)
                                    .child("Likes")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .removeValue();
                            int cnt = Integer.parseInt(post.getPlike()) - 1;
                            post.setPlike(String.valueOf(cnt));
                        } else {
                            // add like
                            FirebaseDatabase.getInstance()
                                    .getReference("Posts")
                                    .child(pid)
                                    .child("Likes")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .setValue(true);
                            int cnt = Integer.parseInt(post.getPlike()) + 1;
                            post.setPlike(String.valueOf(cnt));
                        }
                        // update count both in UI and in RTDB
                        FirebaseDatabase.getInstance()
                                .getReference("Posts")
                                .child(pid)
                                .child("plike")
                                .setValue(post.getPlike());
                        h.likeCountTv.setText(post.getPlike() + " Likes");
                    });
        });

        // 5) Comment button → post details
        h.commentBtn.setOnClickListener(v -> {
            Intent details = new Intent(context, PostDetailsActivity.class);
            details.putExtra("pid", pid);
            details.putExtra("pimage", post.getUimage());
            details.putExtra("pdesc", post.getDescription());
            details.putExtra("authorUid", authorUid);
            context.startActivity(details);
        });

        // 6) More menu (delete/share)
        h.moreBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, h.moreBtn);
            popup.getMenu().add("Delete");
            popup.getMenu().add("Share");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Delete")) {
                    FirebaseDatabase.getInstance()
                            .getReference("Posts")
                            .child(pid)
                            .removeValue();
                } else {
                    Intent share = new Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT,
                                    post.getTitle() + "\n" + post.getDescription());
                    context.startActivity(
                            Intent.createChooser(share, "Share post via")
                    );
                }
                return true;
            });
            popup.show();
        });
    }

    @Override public int getItemCount() {
        return posts.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        ImageView   avatarIv, postImageIv;
        TextView    usernameTv, timeTv, titleTv, descriptionTv;
        TextView    likeCountTv, commentCountTv;
        Button      likeBtn, commentBtn;
        ImageButton moreBtn;

        MyHolder(@NonNull View v) {
            super(v);
            avatarIv        = v.findViewById(R.id.avatarIv);
            usernameTv      = v.findViewById(R.id.usernameTv);
            timeTv          = v.findViewById(R.id.timeTv);
            moreBtn         = v.findViewById(R.id.moreBtn);

            titleTv         = v.findViewById(R.id.titleTv);
            descriptionTv   = v.findViewById(R.id.descriptionTv);
            postImageIv     = v.findViewById(R.id.postImageIv);

            likeCountTv     = v.findViewById(R.id.likeCountTv);
            commentCountTv  = v.findViewById(R.id.commentCountTv);
            likeBtn         = v.findViewById(R.id.likeBtn);
            commentBtn      = v.findViewById(R.id.commentBtn);
        }
    }
}
