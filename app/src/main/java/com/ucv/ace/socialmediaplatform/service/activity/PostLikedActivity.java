package com.ucv.ace.socialmediaplatform.service.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelUsers;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterLikes;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PostLikedActivity extends AppCompatActivity {
    private RecyclerView rvLikedUsers;
    private ProgressBar progress;
    private AdapterLikes adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked);

        // Setup toolbar with back button
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle("Likes");
            ab.setDisplayHomeAsUpEnabled(true);
        }

        rvLikedUsers = findViewById(R.id.rv_liked_users);
        progress = findViewById(R.id.progress_loading);
        rvLikedUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterLikes();
        rvLikedUsers.setAdapter(adapter);

        String postId = getIntent().getStringExtra("EXTRA_POST_ID");
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "ID post invalid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        fetchLikedUsers(postId);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchLikedUsers(String postId) {
        progress.setVisibility(ProgressBar.VISIBLE);
        DatabaseReference likesRef = FirebaseDatabase.getInstance()
                .getReference("Posts").child(postId).child("Likes");

        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists() || snap.getChildrenCount() == 0) {
                    progress.setVisibility(ProgressBar.GONE);
                    Toast.makeText(PostLikedActivity.this,
                            "No likes for this post", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<ModelUsers> users = new ArrayList<>();
                AtomicInteger loaded = new AtomicInteger(0);
                int total = (int) snap.getChildrenCount();
                for (DataSnapshot ds : snap.getChildren()) {
                    String uid = ds.getKey();
                    FirebaseDatabase.getInstance()
                            .getReference("Users").child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot us) {
                                    if (us.exists()) {
                                        ModelUsers user = us.getValue(ModelUsers.class);
                                        users.add(user);
                                    }
                                    if (loaded.incrementAndGet() == total) finishLoading(users);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError e) {
                                    if (loaded.incrementAndGet() == total) finishLoading(users);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                progress.setVisibility(ProgressBar.GONE);
                Toast.makeText(PostLikedActivity.this,
                        "Firebase error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finishLoading(List<ModelUsers> users) {
        progress.setVisibility(ProgressBar.GONE);
        // sort by name
        Collections.sort(users, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        adapter.setUsers(users);
    }
}
