package com.ucv.ace.socialmediaplatform.service.activity;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelPost;
import com.ucv.ace.socialmediaplatform.model.ModelUsers;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterPosts;
import com.ucv.ace.socialmediaplatform.service.post.fragment.ProfileFragment;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ViewUserProfileActivity extends AppCompatActivity {

    private ImageView avatarIv, coverImage;
    private TextView nameTv, friendsCountTv;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        avatarIv = findViewById(R.id.avatariv);
        coverImage = findViewById(R.id.cover_image);
        nameTv = findViewById(R.id.nametv);
        friendsCountTv = findViewById(R.id.friends_count);
        tabLayout = findViewById(R.id.userTabLayout);
        viewPager = findViewById(R.id.userViewPager);

        userId = getIntent().getStringExtra("USER_ID");
        Log.d("DEBUG_PROFILE", "USER_ID received = " + userId);

        if (userId == null) {
            nameTv.setText("User ID missing");
            friendsCountTv.setText("No data");
            return;
        }

        loadUserData(userId);

        ViewUserPagerAdapter adapter = new ViewUserPagerAdapter(this, userId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Posts");
            } else {
                tab.setText("About");
            }
        }).attach();
    }

    private void loadUserData(String userId) {
        Log.d("DEBUG_PROFILE", "Loading user data for: " + userId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("DEBUG_PROFILE", "Snapshot exists = " + snapshot.exists());
                ModelUsers user = snapshot.getValue(ModelUsers.class);
                if (user != null) {
                    nameTv.setText(user.getName() != null ? user.getName() : "No name");

                    Glide.with(ViewUserProfileActivity.this)
                            .load(user.getImage())
                            .placeholder(R.drawable.ic_image)
                            .into(avatarIv);

                    Glide.with(ViewUserProfileActivity.this)
                            .load(user.getCover())
                            .placeholder(R.drawable.cover_placeholder)
                            .into(coverImage);

                    // Count "friends" as all other users
                    FirebaseDatabase.getInstance().getReference("Users")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int count = 0;
                                    for (DataSnapshot snap : snapshot.getChildren()) {
                                        if (!snap.getKey().equals(userId)) count++;
                                    }
                                    friendsCountTv.setText(count + " friends");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    friendsCountTv.setText("Error counting friends");
                                }
                            });

                } else {
                    nameTv.setText("User not found");
                    friendsCountTv.setText("0 friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                nameTv.setText("Error loading user");
                friendsCountTv.setText("Error");
            }
        });
    }

    /**
     * updateFriendsCount method retrieves the total number of users in the Firebase database
     * and updates the friends count TextView.
     * It uses a single value event listener to fetch the data only once.
     * This method counts all users except the current user.
     */
    private void updateFriendsCount() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalUsers = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (!ds.getKey().equals(userId)) {
                        totalUsers++;
                    }
                }
                friendsCountTv.setText(totalUsers + " friends");
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

/***
 * ViewUserPagerAdapter class is a FragmentStateAdapter that manages the fragments for the ViewPager.
 * It creates two fragments: ViewUserPostsFragment and ViewUserAboutFragment.
 */
public class ViewUserPagerAdapter extends FragmentStateAdapter {

    private final String userId;

    public ViewUserPagerAdapter(@NonNull FragmentActivity fragmentActivity, String userId) {
        super(fragmentActivity);
        this.userId = userId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return ViewUserPostsFragment.newInstance(userId);
        } else {
            return ViewUserAboutFragment.newInstance(userId);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}


/**
 * ViewUserPostsFragment class displays the user's posts in a RecyclerView.
 * It retrieves the posts from the Firebase database based on the userId
 * and updates the RecyclerView with the posts.
 */

public static class ViewUserPostsFragment extends Fragment {

    private static final String ARG_UID = "uid";
    private String uid;
    private RecyclerView recyclerView;
    private AdapterPosts adapterPosts;
    private List<ModelPost> postList;

    public static ViewUserPostsFragment newInstance(String uid) {
        ViewUserPostsFragment fragment = new ViewUserPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) uid = getArguments().getString(ARG_UID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        adapterPosts = new AdapterPosts(getContext(), postList);
        recyclerView.setAdapter(adapterPosts);

        loadUserPosts();

        return view;
    }

    private void loadUserPosts() {
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

/**
 * ViewUserAboutFragment class displays the user's about information in a RecyclerView.
 * It retrieves the user's about information from the Firebase database
 * and updates the RecyclerView with the about items.
 * It uses a custom adapter to display the about items.
 * This fragment is used to show the user's bio and website.
 */
public static class ViewUserAboutFragment extends Fragment {

    private static final String ARG_UID = "uid";
    private String uid;
    private RecyclerView recyclerView;
    private ProfileFragment.AboutAdapter aboutAdapter;
    private List<ProfileFragment.AboutItem> aboutItems;

    public static ViewUserAboutFragment newInstance(String uid) {
        ViewUserAboutFragment fragment = new ViewUserAboutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) uid = getArguments().getString(ARG_UID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_about);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        aboutItems = new ArrayList<>();
        aboutAdapter = new ProfileFragment.AboutAdapter(aboutItems);
        recyclerView.setAdapter(aboutAdapter);

        loadAboutData();

        return view;
    }

    private void loadAboutData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers user = snapshot.getValue(ModelUsers.class);
                aboutItems.clear();
                if (user != null) {
                    if (user.getAbout() != null && !user.getAbout().isEmpty()) {
                        aboutItems.add(new ProfileFragment.AboutItem(R.drawable.ic_image, "Bio", user.getAbout()));
                    }
                    if (user.getWebsite() != null && !user.getWebsite().isEmpty()) {
                        aboutItems.add(new ProfileFragment.AboutItem(R.drawable.ic_add, "Website", user.getWebsite()));
                    }
                    aboutAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
}
