package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelUsers;
import com.ucv.ace.socialmediaplatform.service.activity.ViewUserProfileActivity;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterFriends;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdapterFriends adapter;
    private List<ModelUsers> friendsList = new ArrayList<>();
    private DatabaseReference usersRef;

    public FriendsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdapterFriends(friendsList, user -> {
            Intent intent = new Intent(getContext(), ViewUserProfileActivity.class);
            intent.putExtra("USER_ID", user.getUid());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadAllUsers();

        return view;
    }

    private void loadAllUsers() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ModelUsers user = snap.getValue(ModelUsers.class);
                    if (user != null && !snap.getKey().equals(currentUserId)) {
                        user.setUid(snap.getKey());
                        friendsList.add(user);
                        Log.d("FriendsFragment", "Loaded user: " + user.getName());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FriendsFragment", "Failed to load users", error.toException());
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
