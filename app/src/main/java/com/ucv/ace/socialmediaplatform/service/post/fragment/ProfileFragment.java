
package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.service.activity.UserProfilePageActivity;

/**
 * A simple {@link Fragment} subclass.
 * In the ProfileFragment We will be showing the Profile of the user where we will be showing users’ data.
 */
@SuppressWarnings("ALL")
public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ImageView avatartv;
    TextView name, email;
    FloatingActionButton floatingActionButton;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragmentcreating a  view to inflate the layout.
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();

        // getting current user data.
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        // Initialising the text view.
        name = view.findViewById(R.id.nametv);
        floatingActionButton = view.findViewById(R.id.fab);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    // Retrieving Data from firebase.
                    String name = "" + dataSnapshot1.child("name").getValue();
                    String emaill = "" + dataSnapshot1.child("email").getValue();
                    String image = "" + dataSnapshot1.child("image").getValue();

                    // Setting data to our text view.
                    ProfileFragment.this.name.setText(name);
                    email.setText(emaill);
                    try {
                        Glide.with(requireActivity()).load(image).into(avatartv);
                    } catch (Exception e) {
                        Log.e("TAG", "Error occurred while load image due to: ", e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // On click we will open UserProfilePageActivity
        floatingActionButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), UserProfilePageActivity.class)));
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
}