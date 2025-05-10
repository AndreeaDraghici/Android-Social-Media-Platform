// AdapterPosts.java
package com.ucv.ace.socialmediaplatform.service.post.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelPost;
import com.ucv.ace.socialmediaplatform.service.activity.PostDetailsActivity;
import com.ucv.ace.socialmediaplatform.service.activity.PostLikedActivity;
import com.ucv.ace.socialmediaplatform.service.AsyncTaskLoadPhoto;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {
    private Context context;
    private List<ModelPost> modelPostList;
    private String myUid;
    private DatabaseReference postReference;

    public AdapterPosts(Context context, List<ModelPost> modelPostList) {
        this.context = context;
        this.modelPostList = modelPostList;
        myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
    }

    @NonNull @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        ModelPost post = modelPostList.get(position);
        String postId = post.getPid();
        String imageUrl = post.getUimage();

        // Bind text and image
        holder.title.setText(post.getTitle());
        holder.description.setText(post.getDescription());
        holder.time.setText(getDate(post.getPtime()));
        holder.like.setText(post.getPlike() + " Likes");
        holder.comments.setText(post.getPcomments() + " Comments");
        holder.email.setText(post.getUemail());
        new AsyncTaskLoadPhoto(holder.image).execute(imageUrl);

        // Click "X Likes" to open list
        holder.like.setOnClickListener(v -> {
            if (postId == null || postId.isEmpty()) {
                Toast.makeText(context, "ID post invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(context, PostLikedActivity.class);
            i.putExtra("EXTRA_POST_ID", postId);
            context.startActivity(i);
        });

        // Like button toggles Like/Unlike
        holder.likeButton.setOnClickListener(v -> {
            DatabaseReference likesRef = postReference.child(postId).child("Likes");
            DatabaseReference plikeRef = postReference.child(postId).child("plike");
            likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int current = Integer.parseInt(post.getPlike());
                    if (snapshot.hasChild(myUid)) {
                        // unlike
                        likesRef.child(myUid).removeValue();
                        plikeRef.setValue(String.valueOf(current - 1));
                        holder.like.setText((current - 1) + " Likes");
                        post.setPlike(String.valueOf(current - 1));
                    } else {
                        // like
                        likesRef.child(myUid).setValue(true);
                        plikeRef.setValue(String.valueOf(current + 1));
                        holder.like.setText((current + 1) + " Likes");
                        post.setPlike(String.valueOf(current + 1));
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        // Comment opens details
        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailsActivity.class);
            intent.putExtra("pid", postId);
            context.startActivity(intent);
        });

        // More options: delete/share image & text
        holder.more.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.more);
            popup.getMenu().add(0, 0, 0, "Delete");
            popup.getMenu().add(0, 1, 1, "Share");
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == 0) {
                    // Delete post
                    String pidToDelete = post.getPid();
                    postReference.child(pidToDelete).removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // find the item index by pid to avoid index issues
                                    for (int i = 0; i < modelPostList.size(); i++) {
                                        if (modelPostList.get(i).getPid().equals(pidToDelete)) {
                                            modelPostList.remove(i);
                                            notifyItemRemoved(i);
                                            break;
                                        }
                                    }
                                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else if (id == 1) {
                    // Share post with image
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUrl));
                    share.putExtra(Intent.EXTRA_TEXT,
                            post.getTitle() + "\n" + post.getDescription());
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(share, "Share via"));
                }
                return true;
            });
            popup.show();
        });
    }

    @Override public int getItemCount() { return modelPostList.size(); }

    private String getDate(String time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(time));
        return DateFormat.format("dd-MM-yyyy hh:mm aa", cal).toString();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, description, time, like, comments, email;
        Button likeButton, comment;
        ImageButton more;
        MyHolder(View v) {
            super(v);
            image = v.findViewById(R.id.pimagetv);
            title = v.findViewById(R.id.ptitletv);
            description = v.findViewById(R.id.descript);
            time = v.findViewById(R.id.utimetv);
            like = v.findViewById(R.id.plikeb);
            comments = v.findViewById(R.id.pcommentco);
            email = v.findViewById(R.id.uemail);
            likeButton = v.findViewById(R.id.like);
            comment = v.findViewById(R.id.comment);
            more = v.findViewById(R.id.morebtn);
        }
    }
}
