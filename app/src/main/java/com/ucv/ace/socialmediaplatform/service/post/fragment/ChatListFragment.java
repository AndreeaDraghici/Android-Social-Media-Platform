package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelChat;
import com.ucv.ace.socialmediaplatform.service.post.adapter.AdapterChat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatListFragment extends Fragment {

    private static final String ARG_UID = "uid";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    private String uid;
    private String myUid;

    private ImageView profileImage;
    private TextView nameTextView;
    private EditText messageInput;
    private ImageButton sendButton, attachButton;
    private RecyclerView recyclerView;

    private List<ModelChat> chatList;
    private AdapterChat adapterChat;
    private Uri imageUri;

    public static ChatListFragment newInstance(String uid) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getString(ARG_UID);
        }
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        profileImage = view.findViewById(R.id.profiletv);
        nameTextView = view.findViewById(R.id.nameptv);
        messageInput = view.findViewById(R.id.messaget);
        sendButton = view.findViewById(R.id.sendmsg);
        attachButton = view.findViewById(R.id.attachbtn);
        recyclerView = view.findViewById(R.id.chatrecycle);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList = new ArrayList<>();

        loadUserData();
        readMessages();

        sendButton.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(msg);
                messageInput.setText("");
                recyclerView.scrollToPosition(chatList.size() - 1);
            } else {
                Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        attachButton.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void loadUserData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String image = snapshot.child("image").getValue(String.class);

                nameTextView.setText(name);
                Glide.with(requireContext()).load(image).placeholder(R.drawable.ic_image).into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> map = new HashMap<>();
        map.put("sender", myUid);
        map.put("receiver", uid);
        map.put("message", message);
        map.put("timestamp", timestamp);
        map.put("type", "text");
        map.put("isSeen", false);

        ref.push().setValue(map);
    }

    private void sendImageMessage(String imageUrl) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> map = new HashMap<>();
        map.put("sender", myUid);
        map.put("receiver", uid);
        map.put("message", imageUrl);
        map.put("timestamp", timestamp);
        map.put("type", "image");
        map.put("isSeen", false);

        ref.push().setValue(map);
    }

    private void openImagePicker() {
        String[] options = {"Camera", "Gallery"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else if (which == 1) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData();
                uploadImageToFirebase();
            } else if (requestCode == CAMERA_REQUEST && data != null && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUriFromBitmap(photo);
                uploadImageToFirebase();
            }
        }
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), bitmap, "CapturedImage", null);
        return Uri.parse(path);
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference ref = FirebaseStorage.getInstance().getReference("ChatImages")
                    .child(System.currentTimeMillis() + ".jpg");

            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        sendImageMessage(uri.toString());

                        ModelChat tempChat = new ModelChat();
                        tempChat.setSender(myUid);
                        tempChat.setReceiver(uid);
                        tempChat.setMessage(uri.toString());
                        tempChat.setTimestamp(String.valueOf(System.currentTimeMillis()));
                        tempChat.setType("image");
                        tempChat.setSeen(false);
                        chatList.add(tempChat);
                        adapterChat.notifyDataSetChanged();
                        recyclerView.scrollToPosition(chatList.size() - 1);
                    })
            );
        }
    }

    private void readMessages() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat != null  && chat.getSender() != null && chat.getReceiver() != null &&
                            ((chat.getSender().equals(myUid) && chat.getReceiver().equals(uid)) ||
                                    (chat.getSender().equals(uid) && chat.getReceiver().equals(myUid)))) {
                        chatList.add(chat);
                    }
                }
                adapterChat = new AdapterChat(getContext(), chatList, null);
                recyclerView.setAdapter(adapterChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
