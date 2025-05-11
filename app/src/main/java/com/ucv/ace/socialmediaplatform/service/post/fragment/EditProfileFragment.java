package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ucv.ace.socialmediaplatform.R;

import java.util.HashMap;

public class EditProfileFragment extends Fragment {
    private static final int REQUEST_GALLERY = 1001;

    private FrameLayout pickerContainer;
    private ImageView editProfileImage;
    private ImageButton btnChangePhoto;
    private EditText editName;
    private Button saveButton;
    private ProgressDialog progressDialog;

    private Uri selectedImageUri;
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
        // bind views
        pickerContainer   = view.findViewById(R.id.photo_picker_container);
        editProfileImage  = view.findViewById(R.id.edit_profile_image);
        btnChangePhoto    = view.findViewById(R.id.btn_change_photo);
        editName          = view.findViewById(R.id.edit_name);
        saveButton        = view.findViewById(R.id.save_button);
        progressDialog    = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);

        // firebase setup
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef      = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());
        storageRef   = FirebaseStorage.getInstance()
                .getReference("ProfileImages");

        // load current user data
        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String name = snapshot.child("name").getValue(String.class);
                String image = snapshot.child("image").getValue(String.class);
                if (name != null) editName.setText(name);
                if (image != null) {
                    Glide.with(requireContext())
                            .load(image)
                            .placeholder(R.drawable.ic_image)
                            .circleCrop()
                            .into(editProfileImage);
                }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });

        // click to pick image
        View.OnClickListener launchPicker = v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, REQUEST_GALLERY);
        };
        pickerContainer.setOnClickListener(launchPicker);
        editProfileImage.setOnClickListener(launchPicker);
        btnChangePhoto.setOnClickListener(launchPicker);

        // save changes
        saveButton.setOnClickListener(v -> updateProfile());
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_image)
                    .circleCrop()
                    .into(editProfileImage);
        }
    }

    private void updateProfile() {
        String name = editName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Enter name", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Saving...");
        progressDialog.show();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        if (selectedImageUri != null) {
            // upload image
            StorageReference imgRef = storageRef.child(firebaseUser.getUid() + ".jpg");
            imgRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                updates.put("image", uri.toString());
                                userRef.updateChildren(updates)
                                        .addOnCompleteListener(task -> {
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                                                requireActivity().getSupportFragmentManager().popBackStack();
                                            } else {
                                                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            })
                    )
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // only name
            userRef.updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
