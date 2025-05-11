package com.ucv.ace.socialmediaplatform.service.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ucv.ace.socialmediaplatform.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;


/**
 * This activity allows the user to manage their profile, such as updating the name,
 * changing the password, and uploading/changing the profile picture.
 * It handles retrieving and displaying the user information from Firebase and
 * updating the Firebase database with changes.
 */

@SuppressWarnings("deprecation")
public class UserProfilePageActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY = 1001;

    private FrameLayout pickerContainer;
    private ImageView profileImage;
    private ImageButton btnChangePhoto;
    private EditText nameEditText;
    private Button saveButton;

    private Uri imageUri;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);

        // 1) bind views
        pickerContainer = findViewById(R.id.photo_picker_container);
        profileImage = findViewById(R.id.edit_profile_image);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        nameEditText = findViewById(R.id.edit_name);
        saveButton = findViewById(R.id.save_button);
        progressDialog = new ProgressDialog(this);

        // 2) firebase refs
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());
        storageRef = FirebaseStorage.getInstance()
                .getReference("ProfileImages");

        // 3) unified click listener
        View.OnClickListener launchPicker = v -> {
            Intent pick = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, REQUEST_GALLERY);
        };

        pickerContainer.setOnClickListener(launchPicker);
        profileImage.setOnClickListener(launchPicker);
        btnChangePhoto.setOnClickListener(launchPicker);

        // 4) save button
        saveButton.setOnClickListener(v -> updateProfile());

        // 5) load existing data (optional)
        loadUserProfile();
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                String name = snap.child("name").getValue(String.class);
                String image = snap.child("image").getValue(String.class);
                nameEditText.setText(name);
                if (image != null) {
                    Glide.with(UserProfilePageActivity.this)
                            .load(image)
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQUEST_GALLERY
                && res == RESULT_OK
                && data != null
                && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(profileImage);
        }
    }

    private void updateProfile() {

        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Saving…");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(firebaseUser.getUid() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(task -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> saveToDatabase(name, uri.toString())))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveToDatabase(name, null);
        }
    }

    private void saveToDatabase(String name, @Nullable String imageUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        if (imageUrl != null) map.put("image", imageUrl);
        userRef.updateChildren(map)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
