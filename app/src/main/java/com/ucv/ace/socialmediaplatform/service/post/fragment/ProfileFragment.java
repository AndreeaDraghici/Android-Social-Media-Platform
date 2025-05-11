package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.app.ProgressDialog;
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
import com.ucv.ace.socialmediaplatform.service.post.fragment.EditProfileFragment;

public class ProfileFragment extends Fragment {

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private ImageView avatarIv;
    private TextView nameTv, emailTv;
    private FloatingActionButton fab;
    private ProgressDialog progressDialog;

    private final ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snap) {
            // If fragment no longer attached, bail out
            if (!isAdded()) return;

            progressDialog.dismiss();

            if (!snap.exists()) {
                Log.w("ProfileFragment", "User snapshot empty");
                return;
            }

            String name  = snap.child("name" ).getValue(String.class);
            String email = snap.child("email").getValue(String.class);
            String image = snap.child("image").getValue(String.class);

            nameTv.setText(name  != null ? name  : "Unknown");
            emailTv.setText(email != null ? email : "No email");

            // Use the ImageView's own context, safe even if fragment detached later
            Glide.with(avatarIv.getContext())
                    .load(image)
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .circleCrop()
                    .into(avatarIv);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError err) {
            if (!isAdded()) return;
            progressDialog.dismiss();
            Log.e("ProfileFragment", "Failed to load profile", err.toException());
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1) Firebase & refs
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef      = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());

        // 2) Wire views
        avatarIv = view.findViewById(R.id.avatariv);
        nameTv   = view.findViewById(R.id.nametv);
        emailTv  = view.findViewById(R.id.emailtv);
        fab      = view.findViewById(R.id.fab);

        // 3) Create progressDialog *after* view exists
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading profile…");
        progressDialog.setCanceledOnTouchOutside(false);

        // 4) Start listening
        progressDialog.show();
        userRef.addValueEventListener(userListener);

        // 5) Edit screen navigation
        fab.setOnClickListener(v -> {
            if (!isAdded()) return;
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up listener and dialog
        userRef.removeEventListener(userListener);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
}
