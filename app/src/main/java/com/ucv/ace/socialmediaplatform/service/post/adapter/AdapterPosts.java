package com.ucv.ace.socialmediaplatform.service.post.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelPost;
import com.ucv.ace.socialmediaplatform.service.AsyncTaskLoadPhoto;
import com.ucv.ace.socialmediaplatform.service.activity.PostDetailsActivity;
import com.ucv.ace.socialmediaplatform.service.activity.PostLikedActivity;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/***
 * The adapter class for the RecyclerView, contains the posts data.
 */

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {


    // Member variables.
    Context context;
    String myuid;
    private DatabaseReference likeReference, postReference;
    boolean mprocesslike = false;

    List<ModelPost> modelPostList;


    /**
     * Constructor that passes in the post list and the context.
     *
     * @param modelPostList - ArrayList containing the posts.
     * @param context       - Context of the application.
     */
    public AdapterPosts(Context context, List<ModelPost> modelPostList) {
        this.context = context;
        this.modelPostList = modelPostList;
        myuid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        likeReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
    }


    /**
     * Required method for creating the holder objects.
     *
     * @param parent   - The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType - The view type of the new View.
     * @return - The newly created ViewHolder.
     */
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new MyHolder(view);
    }

    /**
     * Method to get the date format time
     *
     * @param time - current time
     * @return -  date format
     */
    private String getDate(String time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(time));
        return DateFormat.format("dd-MM-yyyy hh:mm aa", cal).toString();
    }


    /**
     * Required method that binds the data to the holder.
     *
     * @param holder   The MyHolder into which the data should be put.
     * @param position The adapter position.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, @SuppressLint("RecyclerView") final int position) {

        // Get current data.
        final String uid = modelPostList.get(position).getUid();
        final String title = modelPostList.get(position).getTitle();
        final String description = modelPostList.get(position).getDescription();
        final String ptime = modelPostList.get(position).getPtime();
        String dp = modelPostList.get(position).getUdp();
        String plike = modelPostList.get(position).getPlike();
        String email = modelPostList.get(position).getUemail();
        String comm = modelPostList.get(position).getPcomments();
        final String pid = modelPostList.get(position).getPtime();
        final String image = modelPostList.get(position).getUimage();

        // Populate the textviews with data.
        holder.title.setText(title);
        holder.description.setText(description);
        holder.time.setText(getDate(ptime));
        holder.like.setText(plike + " Likes");
        holder.comments.setText(comm + " Comments");
        holder.email.setText(email);
        holder.image.setVisibility(View.VISIBLE);

        try {

            // Glide.with(context).load(image).into(holder.image);

            AsyncTaskLoadPhoto loadPhoto = new AsyncTaskLoadPhoto(holder.image);
            loadPhoto.execute(image);

        } catch (Exception e) {
            Log.e("TAG", "Error occurred while load image due to: ", e);
        }

        holder.like.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), PostLikedActivity.class);
            intent.putExtra("pid", pid);
            holder.itemView.getContext().startActivity(intent);
        });

        /**
         * We are going to Like a post.
         */
        holder.likeButton.setOnClickListener(v -> {
            final int countingNumberOfLikes = Integer.parseInt(modelPostList.get(position).getPlike());
            mprocesslike = true;
            final String postId = modelPostList.get(position).getPtime();
            likeReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (mprocesslike) {
                        if (dataSnapshot.child(postId).hasChild(myuid)) {
                            postReference.child(postId).child("plike").setValue("" + (countingNumberOfLikes - 1));
                            likeReference.child(postId).child(myuid).removeValue();
                        } else {

                            //increment the number of likes to photo
                            postReference.child(postId).child("plike").setValue("" + (countingNumberOfLikes + 1));
                        }
                        mprocesslike = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        });
        holder.more.setOnClickListener(v -> showMoreOptions(holder.more, uid, myuid, ptime, image));
        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailsActivity.class);
            intent.putExtra("pid", ptime);
            context.startActivity(intent);
        });
    }

    private void showMoreOptions(ImageButton more, String uid, String myuid, final String pid, final String image) {

        /**
         * Posts can only be deleted or sharing by the owner of posts. In the top right of the blog, a button is there.
         * After clicking on that a popup will come in that Delete and Share buttons will come.
         */
        PopupMenu popupMenu = new PopupMenu(context, more, Gravity.END);
        if (uid.equals(myuid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "DELETE");
            popupMenu.getMenu().add(Menu.NONE, 1, 1, "SHARE");
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                /**
                 * After clicking on Delete, the post will be successfully deleted from the posts.
                 */
                deletePost(pid, image);
            }
            if (item.getItemId() == 1) {
                Intent shareIntent = new Intent();
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("ptime").equalTo(pid);

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            ModelPost data = dataSnapshot1.getValue(ModelPost.class);
                            shareIntent.setType("text/plain");
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "Description: " + Objects.requireNonNull(data).getDescription());
                            context.startActivity(shareIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            return false;
        });
        popupMenu.show();
    }

    /**
     * We are going to delete the Post written by the user.
     *
     * @param pid   - post id
     * @param image - post image
     */
    private void deletePost(final String pid, String image) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting the post...");
        StorageReference pictureReferenceFromUrl = FirebaseStorage.getInstance().getReferenceFromUrl(image);
        pictureReferenceFromUrl.delete().addOnSuccessListener(aVoid -> {
            /**
             * We will get the node inside the “Posts” which we want to delete.
             * We are directly deleting the snapshot reference value.
             */
            Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("ptime").equalTo(pid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        //removing the value after getting reference.
                        dataSnapshot1.getRef().removeValue();
                    }
                    pd.dismiss();
                    Toast.makeText(context, "Post was deleted successfully!!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).addOnFailureListener(e -> {

        });
    }


    /**
     * Required method for determining the size of the data set.
     *
     * @return - Size of the data set.
     */
    @Override
    public int getItemCount() {
        return modelPostList.size();
    }


    /**
     * ViewHolder class that represents each row of data in the RecyclerView.
     */
    static class MyHolder extends RecyclerView.ViewHolder {
        ImageView image;

        // Member Variables for the TextViews
        TextView time, title, description, like, comments, email;
        ImageButton more;
        Button likeButton, comment;
        LinearLayout profile;

        /**
         * Constructor for the MyHolder, used in onCreateViewHolder().
         *
         * @param itemView - The root view of the row_posts.xml layout file.
         */
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views.
            image = itemView.findViewById(R.id.pimagetv);
            time = itemView.findViewById(R.id.utimetv);
            more = itemView.findViewById(R.id.morebtn);
            title = itemView.findViewById(R.id.ptitletv);
            description = itemView.findViewById(R.id.descript);
            like = itemView.findViewById(R.id.plikeb);
            comments = itemView.findViewById(R.id.pcommentco);
            likeButton = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            profile = itemView.findViewById(R.id.profilelayout);
            email = itemView.findViewById(R.id.uemail);

        }
    }


}