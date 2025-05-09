package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ucv.ace.socialmediaplatform.R;

import java.io.IOException;
import java.util.HashMap;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private EditText nameEditText;
    private Button saveButton;
    private Uri imageUri;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        profileImage = view.findViewById(R.id.edit_profile_image);
        nameEditText = view.findViewById(R.id.edit_name);
        saveButton = view.findViewById(R.id.save_button);
        progressDialog = new ProgressDialog(getContext());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImages");

        profileImage.setOnClickListener(v -> openImagePicker());

        saveButton.setOnClickListener(v -> updateProfile());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(profileImage);
        }
    }

    private void updateProfile() {
        String name = nameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Introduceți un nume", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Salvare în curs...");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(firebaseUser.getUid() + ".jpg");
            fileRef.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveToDatabase(name, uri.toString());
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Eroare la salvarea imaginii", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveToDatabase(name, null);
        }
    }

    private void saveToDatabase(String name, @Nullable String imageUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        if (imageUrl != null) map.put("image", imageUrl);

        userRef.updateChildren(map).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profil actualizat cu succes", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Eroare la actualizare", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
