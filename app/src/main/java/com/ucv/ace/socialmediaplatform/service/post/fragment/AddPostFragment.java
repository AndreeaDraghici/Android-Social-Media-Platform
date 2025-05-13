package com.ucv.ace.socialmediaplatform.service.post.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.service.board.DashboardActivity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * In the AddPostFragment We will be adding our posts.
 */
@SuppressWarnings("deprecation")
public class AddPostFragment extends Fragment {
    public static final String CHANNEL = "MYCHANNEL";
    private static final int CAMERA_REQUEST = 2;
    private static final int STORAGE_REQUEST = 1;

    String cameraPermission[];
    String storagePermission[];
    ProgressDialog progressDialog;
    ImageView image;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    Uri imageuri = null;

    FirebaseAuth firebaseAuth;
    EditText title, description;
    String name, email, uid, dp;
    DatabaseReference databaseReference;
    Button upload;

    public AddPostFragment() {
        // Required empty public constructor
    }


    /**
     * Initializes the activity.
     *
     * @param savedInstanceState - The current state data.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);
        title = view.findViewById(R.id.ptitle);
        description = view.findViewById(R.id.pdes);
        upload = view.findViewById(R.id.upload);
        image = view.findViewById(R.id.imagep);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCanceledOnTouchOutside(false);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    name = Objects.requireNonNull(dataSnapshot1.child("name").getValue()).toString();
                    email = "" + dataSnapshot1.child("email").getValue();
                    if (dataSnapshot1.child("image").getValue() != null) {
                        dp = dataSnapshot1.child("image").getValue().toString();
                    } else {
                        dp = "";
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });

        /** Initialising camera and storage permission. **/
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        /**  After click on image we will be selecting an image. **/
        image.setOnClickListener(v -> showImagePicDialog());

        upload.setOnClickListener(v -> {
            String title = "" + this.title.getText().toString().trim();
            String description = "" + this.description.getText().toString().trim();

            // If empty set error
            if (TextUtils.isEmpty(title)) {
                this.title.setError("Title can not be empty");
                Toast.makeText(getContext(), "Title can't be empty", Toast.LENGTH_LONG).show();
                return;
            }

            // If empty set error
            if (TextUtils.isEmpty(description)) {
                this.description.setError("Description can not be empty");
                Toast.makeText(getContext(), "Description can't be empty", Toast.LENGTH_LONG).show();
                return;
            }

            // If empty show error
            if (imageuri == null) {
                Toast.makeText(getContext(), "Select an Image", Toast.LENGTH_LONG).show();
            } else {
                uploadData(title, description);
            }
        });
        return view;
    }

    /**
     * Show the dialog box to select the image from camera or storage.
     * If camera is selected then check for the permission. If not given then request for permission.
     */
    private void showImagePicDialog() {
        String[] options = {"Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pick Image From");
        builder.setItems(options, (dialog, which) -> {
            /** check for the camera and storage permission if
             not given the request for permission. **/
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
            } else if (which == 1) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                }
            }
        });
        builder.create().show();
    }

    /**
     * check for storage permission.
     **/
    private Boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    /**
     * if not given then request for permission after that check if request is given or not.
     **/
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0) {
                boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                /** if request access given the pick data. **/
                if (camera_accepted) {
                    pickFromCamera();
                } else {
                    Toast.makeText(getContext(), "Please Enable Camera and Storage Permissions !!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * request for permission to write data into storage.
     **/
    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    /**
     * check camera permission to click picture using camera.
     **/
    private Boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
    }

    /**
     * request for permission to click photo using camera in app.
     **/
    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }

    /**
     * if access is given then pick image from camera and then put
     * the imageuri in intent extra and pass to start activity for result.
     **/
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageuri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(cameraIntent, IMAGE_PICKCAMERA_REQUEST);
    }


    /**
     * Display Notification.
     *
     * @param email - user email.
     * @param title - title of post.
     */
    public void showNotification(String email, String title) {
        /** Sets up the pending intent to update the notification.**/
        Intent intent = new Intent(getContext(), AddPostFragment.class);
        NotificationChannel notificationChannel = null;

        /** Notification channels are only available in version of Android.
         So, add a check on SDK version. **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /** Create the NotificationChannel with all the parameters.**/
            notificationChannel = new NotificationChannel(CHANNEL, "LittleDuck Notification", NotificationManager.IMPORTANCE_HIGH);
        }

        /** Set up the pending intent that is delivered when the notification is clicked.**/
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 1, intent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /** Build the notification with all of the parameters.**/
            notification = new Notification.Builder(getContext(), CHANNEL)
                    .setContentTitle("New post from " + email)
                    .setContentText("A new post was created and posted!")
                    .setContentIntent(pendingIntent)
                    .setChannelId(CHANNEL)
                    .setSmallIcon(android.R.drawable.sym_action_chat)
                    .build();
        }

        /** Create a notification manager object.**/
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        /** Notification channels are only available in version of Android.
         So, add a check on SDK version. **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(notificationChannel);
        }

        /** Deliver the notification.**/
        notificationManager.notify(1, notification);
    }


    /**
     * Upload the value of post data into firebase.
     *
     * @param title       - title of post
     * @param description - title of description
     */
    private void uploadData(final String title, final String description) {

        /** show the progress dialog box.  **/
        progressDialog.setMessage("Publishing Post");
        progressDialog.show();
        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post" + timestamp;
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        /** initialising the storage reference for updating the data. **/
        StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child(filePathName);
        storageReference1.putBytes(data).addOnSuccessListener(taskSnapshot -> {

            /**
             *  We will get the url of our image using uritask. We will get the url of image uploaded.
             */
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl(); /** getting the url of image uploaded. **/
            while (!uriTask.isSuccessful()) ;
            String downloadUri = uriTask.getResult().toString();

            if (uriTask.isSuccessful()) {

                /** if task is successful the update the data into firebase. **/
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("uid", Objects.requireNonNull(user).getUid());
                hashMap.put("uemail", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
                hashMap.put("udp", dp);
                hashMap.put("title", title);
                hashMap.put("description", description);
                hashMap.put("uimage", downloadUri);
                hashMap.put("ptime", timestamp);
                hashMap.put("plike", "0");
                hashMap.put("pcomments", "0");

                /**  set the data into firebase and then empty the title ,description and image data. **/
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                databaseReference.child(timestamp).setValue(hashMap)
                        .addOnSuccessListener(aVoid -> {
                            progressDialog.dismiss();
                            /** Shows a Toast when the post is uploaded with success.**/
                            Toast.makeText(getContext(), "Published", Toast.LENGTH_LONG).show();
                            showNotification(user.getEmail(), this.title.getText().toString());
                            this.title.setText("");
                            this.description.setText("");
                            image.setImageURI(null);
                            imageuri = null;
                            startActivity(new Intent(getContext(), DashboardActivity.class));
                            getActivity().finish();
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed", Toast.LENGTH_LONG).show();
                        });
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Failed", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Here we are getting data from image. If the image is selected then set the image uri.
     **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                image.setImageURI(imageuri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}