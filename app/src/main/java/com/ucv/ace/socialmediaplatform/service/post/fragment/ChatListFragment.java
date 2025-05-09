package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelChat;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterChat;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class ChatListFragment extends Fragment {

    private static final String ARG_UID = "uid";
    private String uid, myUid;
    private ImageView profile;
    private TextView name, status;
    private EditText msg;
    private ImageButton send;
    private RecyclerView recyclerView;
    private AdapterChat adapterChat;
    private List<ModelChat> chatList = new ArrayList<>();

    public static ChatListFragment newInstance(String uid) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat, container, false);

        profile = view.findViewById(R.id.profiletv);
        name = view.findViewById(R.id.nameptv);
        status = view.findViewById(R.id.onlinetv);
        msg = view.findViewById(R.id.messaget);
        send = view.findViewById(R.id.sendmsg);
        recyclerView = view.findViewById(R.id.chatrecycle);

        if (getArguments() != null) {
            uid = getArguments().getString(ARG_UID);
        }
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUserData();
        send.setOnClickListener(v -> sendMessage());

        readMessages();

        return view;
    }

    private void loadUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nameVal = snapshot.child("name").getValue(String.class);
                String image = snapshot.child("image").getValue(String.class);
                name.setText(nameVal);
                Glide.with(requireContext()).load(image).placeholder(R.drawable.ic_image).into(profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String message = msg.getText().toString().trim();
        if (TextUtils.isEmpty(message)) return;

        String timestamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        HashMap<String, Object> map = new HashMap<>();
        map.put("sender", myUid);
        map.put("receiver", uid);
        map.put("message", message);
        map.put("timestamp", timestamp);
        map.put("isSeen", false);
        map.put("type", "text");

        chatRef.push().setValue(map);
        msg.setText("");
    }

    private void readMessages() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat != null && ((chat.getSender().equals(myUid) && chat.getReceiver().equals(uid)) ||
                            (chat.getSender().equals(uid) && chat.getReceiver().equals(myUid)))) {
                        chatList.add(chat);
                    }
                }
                adapterChat = new AdapterChat(requireContext(), chatList, null);
                recyclerView.setAdapter(adapterChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

}
