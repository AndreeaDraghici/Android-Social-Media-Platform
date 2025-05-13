package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
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

import java.util.HashMap;

public class EditProfileFragment extends Fragment {
    private static final int REQUEST_GALLERY = 1001;

    private FrameLayout photoPickerContainer;
    private ImageView profileImageView;
    private ImageButton changePhotoBtn;
    private TextInputEditText nameEdit, emailEdit;
    private Button saveBtn, resetPasswordBtn;
    private ProgressDialog progressDialog;

    private Uri selectedImageUri;
    private String originalName = "";
    private String originalEmail = "";

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoPickerContainer = view.findViewById(R.id.photo_picker_container);
        profileImageView     = view.findViewById(R.id.edit_profile_image);
        changePhotoBtn       = view.findViewById(R.id.btn_change_photo);
        nameEdit             = view.findViewById(R.id.edit_name);
        emailEdit            = view.findViewById(R.id.edit_email);
        saveBtn              = view.findViewById(R.id.save_button);
        resetPasswordBtn     = view.findViewById(R.id.reset_password_button);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef      = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        storageRef   = FirebaseStorage.getInstance().getReference("ProfileImages");

        // Load current user data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                originalName = snapshot.child("name").getValue(String.class);
                originalEmail = firebaseUser.getEmail();

                nameEdit.setText(originalName);
                emailEdit.setText(originalEmail);

                String image = snapshot.child("image").getValue(String.class);
                if (image != null) {
                    Glide.with(requireContext()).load(image).circleCrop().into(profileImageView);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Enable save button only when something changes
        TextWatcher watcher = new SimpleTextWatcher(() -> {
            boolean nameChanged = !TextUtils.isEmpty(nameEdit.getText()) &&
                    !nameEdit.getText().toString().equals(originalName);
            boolean emailChanged = !TextUtils.isEmpty(emailEdit.getText()) &&
                    !emailEdit.getText().toString().equals(originalEmail);
            saveBtn.setEnabled(nameChanged || emailChanged || selectedImageUri != null);
        });

        nameEdit.addTextChangedListener(watcher);
        emailEdit.addTextChangedListener(watcher);

        View.OnClickListener launchPicker = v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, REQUEST_GALLERY);
        };

        photoPickerContainer.setOnClickListener(launchPicker);
        profileImageView.setOnClickListener(launchPicker);
        changePhotoBtn.setOnClickListener(launchPicker);

        saveBtn.setOnClickListener(v -> updateProfile());
        resetPasswordBtn.setOnClickListener(v -> sendResetPasswordEmail());
    }

    private void sendResetPasswordEmail() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Password")
                .setMessage("We'll send a password reset email to your account. Continue?")
                .setPositiveButton("Send", (dialog, which) ->
                        FirebaseAuth.getInstance().sendPasswordResetEmail(firebaseUser.getEmail())
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(getContext(), "Reset email sent", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).circleCrop().into(profileImageView);
            saveBtn.setEnabled(true);
        }
    }

    private void updateProfile() {
        String name = nameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();

        HashMap<String, Object> updates = new HashMap<>();
        boolean changed = false;

        if (!TextUtils.isEmpty(name) && !name.equals(originalName)) {
            updates.put("name", name);
            changed = true;
        }

        progressDialog.setMessage("Saving...");
        progressDialog.show();

        if (selectedImageUri != null) {
            StorageReference imgRef = storageRef.child(firebaseUser.getUid() + ".jpg");
            boolean finalChanged = changed;
            imgRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                updates.put("image", uri.toString());
                                finalizeUpdate(updates, email, finalChanged || true); // include image change
                            }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            finalizeUpdate(updates, email, changed);
        }
    }

    private void finalizeUpdate(HashMap<String, Object> updates, String email, boolean changed) {
        if (!changed && email.equals(originalEmail)) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "No changes detected", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (!email.equals(originalEmail)) {
                firebaseUser.updateEmail(email)
                        .addOnSuccessListener(unused -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Email update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    // SimpleTextWatcher class to reduce boilerplate
    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onTextChanged;

        public SimpleTextWatcher(Runnable onTextChanged) {
            this.onTextChanged = onTextChanged;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextChanged.run();
        }
        @Override public void afterTextChanged(Editable s) {}
    }
}
