package com.ucv.ace.socialmediaplatform.service.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelComment;
import com.ucv.ace.socialmediaplatform.service.board.DashboardActivity;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterComment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/***
 * This activity handles displaying post details, loading comments, and posting new comments.
 * It also manages notifications when a new comment is added and updates the comment count for the post.
 */

public class PostDetailsActivity extends AppCompatActivity {
    /**
     * Channel ID for notifications.
     */
    public static final String CHANNEL = "MYCHANNEL";

    /**
     * User and post data.
     */
    String myuid, myname, myemail, mydp, postId;
    /**
     * UI components for commenting and viewing comments.
     */
    EditText comment;
    ImageButton sendButton;
    RecyclerView recyclerView;

    /**
     * List to hold comments and the adapter for displaying them.
     */
    List<ModelComment> commentList;
    AdapterComment adapterComment;

    /**
     * Action bar for navigation and post details display.
     */
    ActionBar actionBar;

    /**
     * Progress dialog to show during comment uploading.
     */
    ProgressDialog progressDialog;

    /**
     * Initializes the activity.
     *
     * @param savedInstanceState - The current state data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_details);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Post Details");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Get post ID and user information
        postId = getIntent().getStringExtra("pid");
        recyclerView = findViewById(R.id.recyclecomment);
        myemail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        comment = findViewById(R.id.typecommet);
        sendButton = findViewById(R.id.sendcomment);
        progressDialog = new ProgressDialog(this);

        actionBar.setSubtitle(String.format("SignedIn as: %s", myemail));

        // Load and display comments
        loadComment();

        // Set button action to upload comment
        sendButton.setOnClickListener(v -> uploadComment());

    }

    /**
     * Loads comments from Firebase and displays them in a RecyclerView.
     */
    private void loadComment() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelComment modelComment = dataSnapshot1.getValue(ModelComment.class);
                    commentList.add(modelComment);
                    adapterComment = new AdapterComment(getApplicationContext(), commentList, myuid, postId);
                    recyclerView.setAdapter(adapterComment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * Uploads a comment to the Firebase database.
     * Displays a progress dialog and updates the comment count after success.
     */
    private void uploadComment() {

        /** show the progress dialog box. **/
        progressDialog.setMessage("Adding Comment");
        final String comment = this.comment.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(PostDetailsActivity.this, "Empty comment", Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.show();
        String timestamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        // Create a HashMap for comment data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", timestamp);
        hashMap.put("comment", comment);
        hashMap.put("ptime", timestamp);
        hashMap.put("uid", myuid);
        hashMap.put("uemail", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
        hashMap.put("udp", mydp);
        hashMap.put("uname", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        // Add comment to the database
        databaseReference.child(timestamp).setValue(hashMap).addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(PostDetailsActivity.this, "Added Comment !", Toast.LENGTH_LONG).show();
            showNotification(myemail, this.comment.getText().toString());
            this.comment.setText("");
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
            this.comment.setText("");
            updateCommentCount();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(PostDetailsActivity.this, "Failed", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Displays a notification when a new comment is added.
     *
     * @param email - The email of the user who added the comment.
     * @param title - The title or content of the comment.
     */
    public void showNotification(String email, String title) {
        /** Sets up the pending intent to update the notification.**/
        Intent intent = new Intent(getApplicationContext(), PostDetailsActivity.class);
        NotificationChannel notificationChannel = null;

        /** Notification channels are only available in version of Android.
         So, add a check on SDK version. **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /** Create the NotificationChannel with all the parameters.**/
            notificationChannel = new NotificationChannel(CHANNEL, "LittleDuck Notification", NotificationManager.IMPORTANCE_HIGH);
        }

        /** Set up the pending intent that is delivered when the notification is clicked.**/
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /** Build the notification with all of the parameters.**/
            notification = new Notification.Builder(getApplicationContext(), CHANNEL)
                    .setContentTitle("New comment from " + email)
                    .setContentText("A new comment was created and posted !")
                    .setContentIntent(pendingIntent)
                    .setChannelId(CHANNEL)
                    .setSmallIcon(android.R.drawable.sym_action_chat)
                    .build();
        }

        /** Create a notification manager object.**/
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /** Notification channels are only available in version of Android.
         So, add a check on SDK version. **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(notificationChannel);
        }

        /** Deliver the notification.**/
        notificationManager.notify(1, notification);

    }


    /**
     * Updates the comment count for the post in the Firebase database.
     */
    boolean count = false;

    private void updateCommentCount() {
        count = true;
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (count) {
                    String comments = "" + dataSnapshot.child("pcomments").getValue();
                    int newComment = Integer.parseInt(comments) + 1;
                    reference.child("pcomments").setValue("" + newComment);
                    count = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}