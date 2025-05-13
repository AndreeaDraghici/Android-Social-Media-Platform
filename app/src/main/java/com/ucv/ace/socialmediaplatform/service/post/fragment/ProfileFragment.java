package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelPost;
import com.ucv.ace.socialmediaplatform.model.ModelUsers;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterPosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ImageView avatarIv, coverImage;
    private TextView nameTv;
    private ImageButton editProfileBtn;
    private Button viewFriendsBtn;
    private static final int COVER_IMAGE_REQUEST_CODE = 1001;
    private Uri imageUri;

    TextView friendsCountTv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return view;

        String uid = firebaseUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("cover_images");

        avatarIv = view.findViewById(R.id.avatariv);
        coverImage = view.findViewById(R.id.cover_image);
        nameTv = view.findViewById(R.id.nametv);
        editProfileBtn = view.findViewById(R.id.edit_profile_button);
        viewFriendsBtn = view.findViewById(R.id.view_friends_button);
        friendsCountTv = view.findViewById(R.id.friends_count);

        coverImage.setOnClickListener(v -> openImageChooser());

        editProfileBtn.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit()
        );

        viewFriendsBtn.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new UsersFragment())
                        .addToBackStack(null)
                        .commit()
        );

        TabLayout tabLayout = view.findViewById(R.id.profileTabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.profileViewPager);

        ProfilePagerAdapter adapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Posts" : "About");
        }).attach();

        loadUserInfo();
        updateFriendsCount();
        return view;
    }
    private void updateFriendsCount() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalUsers = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (!ds.getKey().equals(firebaseUser.getUid())) {
                        totalUsers++;
                    }
                }
                friendsCountTv.setText(totalUsers + " friends");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Couldn't load friends", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUserInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers user = snapshot.getValue(ModelUsers.class);
                if (user == null) return;

                nameTv.setText(user.getName());
                Context context = getContext();
                if (context != null) {
                    Glide.with(context)
                            .load(user.getImage())
                            .placeholder(R.drawable.ic_image)
                            .circleCrop()
                            .into(avatarIv);
                }

                String coverUrl = user.getCover();
                if (coverUrl != null && !coverUrl.isEmpty() && context != null) {
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
                    Toast.makeText(context, "Couldn't load friends", Toast.LENGTH_SHORT).show();
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

    private static class ProfilePagerAdapter extends FragmentStateAdapter {
        public ProfilePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new PostsFragment() : new AboutFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    public static class PostsFragment extends Fragment {
        private RecyclerView recyclerView;
        private List<ModelPost> postList;
        private AdapterPosts adapterPosts;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            RecyclerView recyclerView = new RecyclerView(requireContext());
            recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            postList = new ArrayList<>();
            adapterPosts = new AdapterPosts(getContext(), postList);
            recyclerView.setAdapter(adapterPosts);

            loadUserPosts();
            return recyclerView;
        }

        private void loadUserPosts() {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.orderByChild("uid").equalTo(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            postList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModelPost post = ds.getValue(ModelPost.class);
                                if (post != null) {
                                    post.setPid(ds.getKey());
                                    postList.add(post);
                                }
                            }
                            adapterPosts.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }
    }

    public static class AboutFragment extends Fragment {
        private FrameLayout container;
        private RecyclerView recyclerView;
        private AboutAdapter aboutAdapter;
        private List<AboutItem> aboutItems;

        private View formView;
        private EditText aboutInput, websiteInput;
        private Button saveButton;
        private ImageButton floatingEditButton;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup parent, Bundle savedInstanceState) {
            container = new FrameLayout(requireContext());
            container.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            aboutItems = new ArrayList<>();
            aboutAdapter = new AboutAdapter(aboutItems);

            recyclerView = new RecyclerView(requireContext());
            recyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(aboutAdapter);

            container.addView(recyclerView);
            loadUserInfo();

            return container;
        }

        private void loadUserInfo() {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ModelUsers user = snapshot.getValue(ModelUsers.class);
                    aboutItems.clear();

                    boolean hasData = false;
                    if (user != null) {
                        if (user.getAbout() != null && !user.getAbout().isEmpty()) {
                            aboutItems.add(new AboutItem(R.drawable.ic_image, "Bio", user.getAbout()));
                            hasData = true;
                        }
                        if (user.getWebsite() != null && !user.getWebsite().isEmpty()) {
                            aboutItems.add(new AboutItem(R.drawable.ic_add, "Website", user.getWebsite()));
                            hasData = true;
                        }
                    }

                    showRecyclerView();
                    if (hasData) {
                        addFloatingEditButton();
                    } else {
                        showForm("", "");
                    }
                    aboutAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        private void showRecyclerView() {
            container.removeAllViews();
            container.addView(recyclerView);
        }

        private void showForm(String about, String website) {
            container.removeAllViews();
            formView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_about, container, false);

            aboutInput = formView.findViewById(R.id.about_input);
            websiteInput = formView.findViewById(R.id.website_input);
            saveButton = formView.findViewById(R.id.save_button);

            aboutInput.setText(about);
            websiteInput.setText(website);

            saveButton.setOnClickListener(v -> saveUserInfo());

            container.addView(formView);
        }

        private void saveUserInfo() {
            String about = aboutInput.getText().toString().trim();
            String website = websiteInput.getText().toString().trim();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            HashMap<String, Object> updates = new HashMap<>();
            updates.put("about", about);
            updates.put("website", website);

            userRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Info saved", Toast.LENGTH_SHORT).show();
                        aboutItems.clear();
                        if (!about.isEmpty()) {
                            aboutItems.add(new AboutItem(R.drawable.ic_image, "Bio", about));
                        }
                        if (!website.isEmpty()) {
                            aboutItems.add(new AboutItem(R.drawable.ic_add, "Website", website));
                        }
                        showRecyclerView();
                        aboutAdapter.notifyDataSetChanged();
                        addFloatingEditButton();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        private void addFloatingEditButton() {
            if (floatingEditButton != null) {
                container.removeView(floatingEditButton);
            }

            floatingEditButton = new ImageButton(requireContext());
            floatingEditButton.setImageResource(R.drawable.ic_edit_white);
            floatingEditButton.setBackgroundResource(R.drawable.floating_button_background);
            floatingEditButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            floatingEditButton.setPadding(16, 16, 16, 16);

            int size = (int) (56 * getResources().getDisplayMetrics().density);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            int margin = (int) (16 * getResources().getDisplayMetrics().density);
            params.gravity = Gravity.END | Gravity.BOTTOM;

            // Ridicăm mai mult butonul pentru a nu fi sub navbar
            params.setMargins(margin, margin, margin, margin + 48); // +48dp în plus
            floatingEditButton.setLayoutParams(params);

            floatingEditButton.setOnClickListener(v -> {
                String currentAbout = "", currentWebsite = "";
                for (AboutItem item : aboutItems) {
                    if ("Bio".equals(item.label)) currentAbout = item.value;
                    if ("Website".equals(item.label)) currentWebsite = item.value;
                }
                showForm(currentAbout, currentWebsite);
            });

            container.addView(floatingEditButton);
        }

    }


    public static class AboutItem {
        public int iconRes;
        public String label;
        public String value;

        public AboutItem(int iconRes, String label, String value) {
            this.iconRes = iconRes;
            this.label = label;
            this.value = value;
        }
    }

    public static class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.AboutViewHolder> {
        private final List<AboutItem> items;

        public AboutAdapter(List<AboutItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public AboutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.about_item, parent, false);
            return new AboutViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AboutViewHolder holder, int position) {
            AboutItem item = items.get(position);
            holder.icon.setImageResource(item.iconRes);
            holder.label.setText(item.label);
            holder.value.setText(item.value);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class AboutViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView label, value;

            public AboutViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                label = itemView.findViewById(R.id.label);
                value = itemView.findViewById(R.id.value);
            }
        }
    }
}




