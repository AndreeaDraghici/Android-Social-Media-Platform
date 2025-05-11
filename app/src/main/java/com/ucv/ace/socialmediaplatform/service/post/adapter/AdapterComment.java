package com.ucv.ace.socialmediaplatform.service.post.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelComment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.Holder> {
    private final Context context;
    private final List<ModelComment> comments;
    private final String myUid;
    private final DatabaseReference postRef;
    private final DatabaseReference usersRef;

    public AdapterComment(Context ctx, List<ModelComment> comments, String myUid, String postId) {
        this.context = ctx;
        this.comments = comments;
        this.myUid = myUid;
        this.postRef = FirebaseDatabase.getInstance()
                .getReference("Posts").child(postId);
        this.usersRef = FirebaseDatabase.getInstance()
                .getReference("Users");
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.row_comments, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        ModelComment c = comments.get(pos);
        h.commentText.setText(c.getComment());
        String rawTs = c.getPtime();
        long ts = 0;
        try {
            ts = Long.parseLong(rawTs);
        } catch (NumberFormatException ignored) {
        }

        // Format however you like:
        String formatted = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
                .format(new Date(ts));

        h.commentTime.setText(formatted);

        // load commenter profile
        String uid = c.getUid();
        if (uid != null && !uid.isEmpty()) {
            usersRef.child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            if (!snap.exists()) return;
                            String name = snap.child("name").getValue(String.class);
                            String avatar = snap.child("image").getValue(String.class);
                            h.commenterName.setText(name != null ? name : "");
                            Glide.with(context)
                                    .load(avatar)
                                    .placeholder(R.drawable.ic_image)
                                    .into(h.commenterAvatar);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {
                        }
                    });
        } else {
            h.commenterName.setText("");
            h.commenterAvatar.setImageResource(R.drawable.ic_image);
        }

        // tap to edit/delete own comments
        if (uid != null && uid.equals(myUid)) {
            h.itemView.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(context, v);
                menu.getMenu().add(0, 0, 0, "Edit");
                menu.getMenu().add(0, 1, 1, "Delete");
                menu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) showEditDialog(c, pos);
                    else deleteComment(c, pos);
                    return true;
                });
                menu.show();
            });
        } else {
            h.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private void deleteComment(ModelComment c, int pos) {
        postRef.child("Comments").child(c.getUid())
                .removeValue()
                .addOnSuccessListener(a -> {
                    // decrement count
                    postRef.child("pcomments")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    Object raw = snap.getValue();
                                    long cnt = 0;
                                    if (raw instanceof Long) cnt = (Long) raw;
                                    else if (raw instanceof String) {
                                        try {
                                            cnt = Long.parseLong((String) raw);
                                        } catch (Exception ignored) {
                                        }
                                    }
                                    postRef.child("pcomments")
                                            .setValue(String.valueOf(Math.max(0, cnt - 1)));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError e) {
                                }
                            });
                    comments.remove(pos);
                    notifyItemRemoved(pos);
                    Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show());
    }

    private void showEditDialog(ModelComment c, int pos) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle("Edit Comment");

        // 1) Build the EditText
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(c.getComment());
        b.setView(input);

        // 2) “Save” button
        b.setPositiveButton("Save", (dlg, which) -> {
            String updatedText = input.getText().toString().trim();
            if (updatedText.isEmpty()) {
                Toast.makeText(context, "Cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3) Only update the "comment" field of the existing node
            Map<String, Object> updates = new HashMap<>();
            updates.put("comment", updatedText);
            // (Optional) update timestamp:
            // updates.put("ptime", String.valueOf(System.currentTimeMillis()));

            postRef.child("Comments")
                    .child(c.getcId())
                    .updateChildren(updates)
                    .addOnSuccessListener(a -> {
                        // 4) Reflect change in-place
                        c.setComment(updatedText);
                        notifyItemChanged(pos);
                        Toast.makeText(context, "Comment updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Edit failed", Toast.LENGTH_SHORT).show()
                    );
        });

        // 5) “Cancel” button
        b.setNegativeButton("Cancel", (dlg, which) -> dlg.dismiss());
        b.show();
    }


    static class Holder extends RecyclerView.ViewHolder {
        ImageView commenterAvatar;
        TextView commenterName, commentText, commentTime;

        Holder(View v) {
            super(v);
            commenterAvatar = v.findViewById(R.id.ivCommenterAvatar);
            commenterName = v.findViewById(R.id.tvCommenterName);
            commentText = v.findViewById(R.id.comment_text);
            commentTime = v.findViewById(R.id.comment_time);
        }
    }
}
