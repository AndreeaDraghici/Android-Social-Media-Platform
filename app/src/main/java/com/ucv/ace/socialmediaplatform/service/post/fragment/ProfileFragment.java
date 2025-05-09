package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.service.activity.UserProfilePageActivity;

public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private ImageView avatartv;
    private TextView name, email;
    private FloatingActionButton floatingActionButton;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        name = view.findViewById(R.id.nametv);
        email = view.findViewById(R.id.emailtv);
        avatartv = view.findViewById(R.id.avatariv);
        floatingActionButton = view.findViewById(R.id.fab);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserData();

        floatingActionButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });



        return view;
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nameValue = snapshot.child("name").getValue(String.class);
                    String emailValue = snapshot.child("email").getValue(String.class);
                    String imageUrl = snapshot.child("image").getValue(String.class);

                    name.setText(nameValue != null ? nameValue : "Nume necunoscut");
                    email.setText(emailValue != null ? emailValue : "Email necunoscut");

                    try {
                        Glide.with(requireActivity()).load(imageUrl).placeholder(R.drawable.ic_image).into(avatartv);
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Eroare la încărcarea imaginii", e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Eroare la citirea datelor", error.toException());
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
}
