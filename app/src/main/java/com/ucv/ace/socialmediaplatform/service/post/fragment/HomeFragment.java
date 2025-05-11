package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelPost;
import com.ucv.ace.socialmediaplatform.service.SplashScreen;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterPosts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private List<ModelPost> posts;
    private AdapterPosts adapterPosts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.postrecyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);

        posts = new ArrayList<>();
        adapterPosts = new AdapterPosts(getActivity(), posts);
        recyclerView.setAdapter(adapterPosts);

        loadPosts();
        return view;
    }

    private void loadPosts() {
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dsRoot) {
                posts.clear();
                for (DataSnapshot ds : dsRoot.getChildren()) {
                    ModelPost m = new ModelPost();
                    m.setPid(ds.getKey());

                    // Read each field explicitly
                    m.setTitle(ds.child("title").getValue(String.class));
                    m.setDescription(ds.child("description").getValue(String.class));
                    m.setUimage(ds.child("uimage").getValue(String.class));
                    m.setPtime(ds.child("ptime").getValue(String.class));
                    m.setUemail(ds.child("uemail").getValue(String.class));
                    m.setUid(ds.child("uid").getValue(String.class));

                    // pcomments as String
                    Object pc = ds.child("pcomments").getValue();
                    String pcomments = "0";
                    if (pc instanceof Long) pcomments = String.valueOf((Long) pc);
                    else if (pc instanceof String) pcomments = (String) pc;
                    m.setPcomments(pcomments);

                    // plike as String
                    Object pl = ds.child("plike").getValue();
                    String plike = "0";
                    if (pl instanceof Long) plike = String.valueOf((Long) pl);
                    else if (pl instanceof String) plike = (String) pl;
                    m.setPlike(plike);

                    posts.add(m);
                }
                adapterPosts.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Context ctx = getContext();
                if (ctx != null)
                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchPosts(final String query) {
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dsRoot) {
                posts.clear();
                for (DataSnapshot ds : dsRoot.getChildren()) {
                    String title = ds.child("title").getValue(String.class);
                    String description = ds.child("description").getValue(String.class);
                    if (title != null && description != null &&
                            (title.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) ||
                                    description.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))) {

                        ModelPost m = new ModelPost();
                        m.setPid(ds.getKey());
                        m.setTitle(title);
                        m.setDescription(description);
                        // copy other fields the same way…
                        Object pc = ds.child("pcomments").getValue();
                        String pcomments = pc instanceof Long
                                ? String.valueOf((Long) pc)
                                : (pc instanceof String ? (String) pc : "0");
                        m.setPcomments(pcomments);

                        Object pl = ds.child("plike").getValue();
                        String plike = pl instanceof Long
                                ? String.valueOf((Long) pl)
                                : (pl instanceof String ? (String) pl : "0");
                        m.setPlike(plike);

                        m.setUimage(ds.child("uimage").getValue(String.class));
                        m.setPtime(ds.child("ptime").getValue(String.class));
                        m.setUemail(ds.child("uemail").getValue(String.class));
                        m.setUid(ds.child("uid").getValue(String.class));

                        posts.add(m);
                    }
                }
                adapterPosts.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Context ctx = getContext();
                if (ctx != null)
                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem mi = menu.findItem(R.id.search);
        SearchView sv = (SearchView) mi.getActionView();
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                if (!TextUtils.isEmpty(q)) searchPosts(q);
                else loadPosts();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String q) {
                if (!TextUtils.isEmpty(q)) searchPosts(q);
                else loadPosts();
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            firebaseAuth.signOut();
            Intent i = new Intent(requireContext(), SplashScreen.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
