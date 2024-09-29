package com.ucv.ace.socialmediaplatform.service.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.service.Utils;

import java.util.HashMap;
import java.util.Objects;


/**
 * This activity allows the user to manage their profile, such as updating the name,
 * changing the password, and uploading/changing the profile picture.
 * It handles retrieving and displaying the user information from Firebase and
 * updating the Firebase database with changes.
 */

@SuppressWarnings("deprecation")
public class UserProfilePageActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String userId;
    ImageView settingProfileImage;
    TextView editName, editPassword;
    ProgressDialog progressDialog;

    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    /**
     * Called when the activity is created. Initializes Firebase instances,
     * sets up the profile data, and handles profile picture changes and name/password updates.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);

        // Initialize UI components
        editName = findViewById(R.id.editname);
        settingProfileImage = findViewById(R.id.setting_profile_image);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        editPassword = findViewById(R.id.changepassword);

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = firebaseDatabase.getReference("Users");

        /**
         * Loads the user's profile image from the Firebase database.
         */
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String image = "" + dataSnapshot1.child("image").getValue();
                    try {
                        Utils utils = new Utils();
                        Glide.with(UserProfilePageActivity.this).load(utils.decodeBitmapFromBase64(image)).into(settingProfileImage);
                    } catch (Exception e) {
                        Log.e("TAG", "Error occurred while load image due to: ", e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Set up listeners for editing profile name and password
        editPassword.setOnClickListener(v -> {
            progressDialog.setMessage("Changing Password");
            showPasswordChangeDialog();
        });

        editName.setOnClickListener(v -> {
            progressDialog.setMessage("Updating Name");
            showNameUpdate("name");
        });


        //Camera permission
        Button cameraButton = findViewById(R.id.take_picture_button);
        cameraButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, CAMERA_PERMISSION_CODE);
            }
        });
    }


    /**
     * Called when the user responds to a permission request. If the camera permission is granted,
     * the camera app is opened.
     *
     * @param requestCode  The request code passed in the requestPermissions() call.
     * @param permissions  The requested permissions.
     * @param grantResults The results for the requested permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accepted
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handles the result from the camera activity. Sets the profile image to the picture taken by the user.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be attached to the intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                assert data != null;
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                // Set the image to an ImageView
                ImageView profileImage = findViewById(R.id.setting_profile_image);
                profileImage.setImageBitmap(imageBitmap);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String image = "" + dataSnapshot1.child("image").getValue();
                    try {
                        Glide.with(UserProfilePageActivity.this).load(image).into(settingProfileImage);
                    } catch (Exception e) {
                        Log.e("TAG", "Error occurred while load image due to: ", e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        editPassword.setOnClickListener(v -> {
            progressDialog.setMessage("Changing Password");
            showPasswordChangeDialog();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String image = "" + dataSnapshot1.child("image").getValue();

                    try {
                        Glide.with(UserProfilePageActivity.this).load(image).into(settingProfileImage);
                    } catch (Exception e) {
                        Log.e("TAG", "Error occurred while load image due to: ", e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        editPassword.setOnClickListener(v -> {
            progressDialog.setMessage("Changing Password");
            showPasswordChangeDialog();
        });
    }

    /**
     * Shows a dialog for changing the password. Prompts the user for their current password
     * and new password, then updates the password in Firebase.
     */
    private void showPasswordChangeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final EditText oldPassword = view.findViewById(R.id.oldpasslog);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) final EditText newPassword = view.findViewById(R.id.newpasslog);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button editPass = view.findViewById(R.id.updatepass);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        editPass.setOnClickListener(v -> {
            String oldPass = oldPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();
            if (TextUtils.isEmpty(oldPass)) {
                Toast.makeText(UserProfilePageActivity.this, "Current Password cant be empty", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(newPass)) {
                Toast.makeText(UserProfilePageActivity.this, "New Password cant be empty", Toast.LENGTH_LONG).show();
                return;
            }
            dialog.dismiss();
            updatePassword(oldPass, newPass);
        });
    }

    /**
     * Updates the user's password in Firebase.
     *
     * @param oldP The user's current password.
     * @param newP The new password to set.
     */

    private void updatePassword(String oldP, final String newP) {
        progressDialog.show();
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(Objects.requireNonNull(Objects.requireNonNull(user).getEmail()), oldP);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(aVoid -> user.updatePassword(newP)
                        .addOnSuccessListener(aVoid1 -> {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfilePageActivity.this, "Changed Password", Toast.LENGTH_LONG).show();
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfilePageActivity.this, "Failed", Toast.LENGTH_LONG).show();
                        })).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UserProfilePageActivity.this, "Failed", Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Shows a dialog for updating the user's name. After entering the new name,
     * it is updated in the Firebase database.
     *
     * @param key The database key to update (in this case, the "name" field).
     */
    private void showNameUpdate(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update" + key);

        // creating a layout to write the new name
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(this);
        editText.setHint("Enter" + key);
        layout.addView(editText);
        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            final String value = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(value)) {
                progressDialog.show();

                // Update the name in Firebase
                HashMap<String, Object> result = new HashMap<>();
                result.put(key, value);
                databaseReference.child(firebaseUser.getUid()).updateChildren(result).addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    // after updated we will show updated
                    Toast.makeText(UserProfilePageActivity.this, " Updated ", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UserProfilePageActivity.this, "Unable to update", Toast.LENGTH_LONG).show();
                });

                // Update the name in user's posts
                if (key.equals("name")) {
                    final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("Posts");
                    Query query = dbReference.orderByChild("uid").equalTo(userId);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                dataSnapshot1.getRef().child("uname").setValue(value);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            } else {
                Toast.makeText(UserProfilePageActivity.this, "Unable to update", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> progressDialog.dismiss());
        builder.create().show();
    }
}