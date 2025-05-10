package com.ucv.ace.socialmediaplatform.service.post.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelComment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.Holder> {
    private final Context context;
    private final List<ModelComment> comments;
    private final String myUid;
    private final String postId;
    private final DatabaseReference postRef;

    public AdapterComment(Context ctx, List<ModelComment> comments, String myUid, String postId) {
        this.context = ctx;
        this.comments = comments;
        this.myUid = myUid;
        this.postId = postId;
        this.postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        ModelComment c = comments.get(i);
        h.commentText.setText(c.getComment());
        h.commentTime.setText(c.getPtime());

        // LONG‐PRESS to delete *your* comment
        if (c.getUid().equals(myUid)) {
            h.itemView.setOnLongClickListener(v -> {
                PopupMenu menu = new PopupMenu(context, v);
                menu.getMenu().add("Delete Comment");
                menu.setOnMenuItemClickListener(item -> {
                    // remove comment node
                    postRef.child("Comments").child(c.getUid()).removeValue()
                            .addOnSuccessListener(a -> {
                                // decrement pcomments
                                postRef.child("pcomments")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snap) {
                                                long cnt = snap.exists() ? snap.getValue(Long.class) : 0L;
                                                postRef.child("pcomments").setValue(cnt > 0 ? cnt - 1 : 0);
                                            }
                                            @Override public void onCancelled(@NonNull DatabaseError e) {}
                                        });
                                Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                                // remove from local list & update UI
                                comments.remove(i);
                                notifyItemRemoved(i);
                            })
                            .addOnFailureListener(e -> Toast.makeText(context,"Delete failed",Toast.LENGTH_SHORT).show());
                    return true;
                });
                menu.show();
                return true;
            });
        } else {
            h.itemView.setOnLongClickListener(null);
        }
    }

    @Override public int getItemCount() {
        return comments.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView commentText, commentTime;
        Holder(View iv) {
            super(iv);
            commentText = iv.findViewById(R.id.comment_text);
            commentTime = iv.findViewById(R.id.comment_time);
        }
    }
}
