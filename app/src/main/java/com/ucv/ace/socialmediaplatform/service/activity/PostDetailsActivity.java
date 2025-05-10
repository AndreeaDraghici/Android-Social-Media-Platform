package com.ucv.ace.socialmediaplatform.service.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelComment;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterComment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostDetailsActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY = 1001;

    private ImageView ivPostImage;
    private TextView tvPostText;
    private ImageButton btnMore, btnAttach, btnSend;
    private EditText etComment;
    private NestedScrollView scrollComments;
    private RecyclerView rvComments;

    private List<ModelComment> commentList = new ArrayList<>();
    private AdapterComment adapterComment;

    private String postId, myUid;
    private Uri attachedImageUri;
    private ProgressDialog progressDialog;
    private DatabaseReference postRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        // enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // read intent extras
        postId = getIntent().getStringExtra("pid");
        String postImageUrl = getIntent().getStringExtra("pimage");
        String postText = getIntent().getStringExtra("pdesc");

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        progressDialog = new ProgressDialog(this);
        postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        // bind views
        ivPostImage = findViewById(R.id.post_image);
        tvPostText = findViewById(R.id.post_text);
        scrollComments = findViewById(R.id.scroll_comments);
        rvComments = findViewById(R.id.recyclecomment);
        btnAttach = findViewById(R.id.btn_attach_photo);
        etComment = findViewById(R.id.typecomment);
        btnSend = findViewById(R.id.sendcomment);

        // load post content
        tvPostText.setText(postText);
        Glide.with(this)
                .load(postImageUrl)
                .placeholder(R.drawable.ic_image)
                .into(ivPostImage);

        // comments RecyclerView
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        adapterComment = new AdapterComment(this, commentList, myUid, postId);
        rvComments.setAdapter(adapterComment);
        loadComments();

        // attach image
        btnAttach.setOnClickListener(v -> startActivityForResult(
                new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                REQUEST_GALLERY));

        // send comment
        btnSend.setOnClickListener(v -> sendComment());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadComments() {
        postRef.child("Comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                commentList.clear();
                for (DataSnapshot ds : snap.getChildren()) {
                    commentList.add(ds.getValue(ModelComment.class));
                }
                adapterComment.notifyDataSetChanged();
                if (scrollComments != null)
                    scrollComments.post(() -> scrollComments.fullScroll(View.FOCUS_DOWN));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void sendComment() {
        String text = etComment.getText().toString().trim();
        if (text.isEmpty() && attachedImageUri == null) {
            Toast.makeText(this, "Enter text or attach a photo", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Posting comment…");
        progressDialog.show();
        String ts = String.valueOf(System.currentTimeMillis());

        if (attachedImageUri != null) {
            StorageReference imgRef = FirebaseStorage.getInstance()
                    .getReference("CommentImages").child(ts + ".jpg");
            imgRef.putFile(attachedImageUri)
                    .addOnSuccessListener(task -> imgRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> writeComment(ts, text, uri.toString())))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            writeComment(ts, text, null);
        }
    }

    private void writeComment(String id, String text, @Nullable String imageUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("cId", id);
        map.put("comment", text);
        map.put("ptime", id);
        map.put("uid", myUid);
        if (imageUrl != null) map.put("commentImage", imageUrl);

        postRef.child("Comments").child(id).setValue(map)
                .addOnSuccessListener(a -> {
                    // increment pcomments correctly
                    postRef.child("pcomments")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    Object raw = snap.exists() ? snap.getValue() : null;
                                    long cnt = 0;
                                    if (raw instanceof Long) {
                                        cnt = (Long) raw;
                                    } else if (raw instanceof String) {
                                        try {
                                            cnt = Long.parseLong((String) raw);
                                        } catch (NumberFormatException ignored) {
                                        }
                                    }
                                    postRef.child("pcomments")
                                            .setValue(String.valueOf(cnt + 1));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError e) {
                                }
                            });
                    progressDialog.dismiss();
                    etComment.setText("");
                    attachedImageUri = null;
                    loadComments();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            attachedImageUri = data.getData();
        }
    }
}
