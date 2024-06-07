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
 * We are going to edit our profile data like changing name, changing the password of the user, and changing profile pic.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);
        editName = findViewById(R.id.editname);
        settingProfileImage = findViewById(R.id.setting_profile_image);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        editPassword = findViewById(R.id.changepassword);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = firebaseDatabase.getReference("Users");
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
     * Updating name
     *
     * @param key
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

                // Here we are updating the new name
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