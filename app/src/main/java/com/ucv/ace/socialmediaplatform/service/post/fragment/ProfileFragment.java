package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelPost;
import com.ucv.ace.socialmediaplatform.model.ModelUsers;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterPosts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class ProfileFragment extends Fragment {

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    private ImageView avatarIv, coverImage;
    private TextView nameTv, emailTv, friendsCountTv;
    private ImageButton editProfileBtn;
    private Button viewFriendsBtn;
    private RecyclerView recyclerView;

    private List<ModelPost> postList;
    private AdapterPosts adapterPosts;

    private static final int COVER_IMAGE_REQUEST_CODE = 1001;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Init Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return view;

        String uid = firebaseUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("cover_images");

        // Bind UI
        coverImage = view.findViewById(R.id.cover_image);
        avatarIv = view.findViewById(R.id.avatariv);
        nameTv = view.findViewById(R.id.nametv);
        friendsCountTv = view.findViewById(R.id.friends_count);
        editProfileBtn = view.findViewById(R.id.edit_profile_button);
        viewFriendsBtn = view.findViewById(R.id.view_friends_button);
        recyclerView = view.findViewById(R.id.userPostsRecyclerView);

        // Posts setup
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList = new ArrayList<>();
        adapterPosts = new AdapterPosts(getContext(), postList);
        recyclerView.setAdapter(adapterPosts);
        loadUserPosts();

        // Cover image click
        coverImage.setOnClickListener(v -> openImageChooser());

        // Edit profile
        editProfileBtn.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit());

        // View friends
        viewFriendsBtn.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new UsersFragment())
                        .addToBackStack(null)
                        .commit());

        // Load user info (name, avatar, cover)
        loadUserInfo();

        return view;
    }

    private void loadUserInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers user = snapshot.getValue(ModelUsers.class);
                if (user == null) return;

                nameTv.setText(user.getName());
                friendsCountTv.setText("0 friends");

                Glide.with(requireContext())
                        .load(user.getImage())
                        .placeholder(R.drawable.ic_image)
                        .circleCrop()
                        .into(avatarIv);

                String coverUrl = user.getCover();
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(coverUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(coverImage);
                } else {
                    coverImage.setImageResource(R.drawable.cover_placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Cover Image"), COVER_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COVER_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadCoverImageToFirebase(imageUri);
        }
    }

    private void uploadCoverImageToFirebase(Uri uri) {
        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading image...");
        pd.show();

        StorageReference fileRef = storageRef.child(firebaseUser.getUid() + ".jpg");
        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String downloadUrl = downloadUri.toString();
                            userRef.child("cover").setValue(downloadUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        pd.dismiss();
                                        Toast.makeText(getContext(), "Cover image updated", Toast.LENGTH_SHORT).show();
                                    });
                        }))
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserPosts() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost post = ds.getValue(ModelPost.class);
                    if (post != null && post.getUid().equals(firebaseUser.getUid())) {
                        post.setPid(ds.getKey());
                        postList.add(post);
                    }
                }
                adapterPosts.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
