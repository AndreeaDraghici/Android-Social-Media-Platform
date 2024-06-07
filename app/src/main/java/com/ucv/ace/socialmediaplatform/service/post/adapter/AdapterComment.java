package com.ucv.ace.socialmediaplatform.service.post.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.model.ModelComment;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;



/***
 * The adapter class for the RecyclerView, contains the comments data.
 */

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyHolder> {

    // Member variables.
    Context context;
    List<ModelComment> list;

    String myuid;
    String postid;

    /**
     * Constructor that passes in the comments and the context.
     *
     * @param context  - Context of the application.
     * @param list     - ArrayList containing the comments.
     * @param myuid    - User ID.
     * @param postid   - Post ID.
     */
    public AdapterComment(Context context, List<ModelComment> list, String myuid, String postid) {
        this.context = context;
        this.list = list;
        this.myuid = myuid;
        this.postid = postid;
    }

    /**
     * Required method for creating the holder objects.
     *
     * @param parent   - The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType - The view type of the new View.
     * @return         - The newly created ViewHolder.
     */

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    /**
     * Required method that binds the data to the holder.
     *
     * @param holder   The MyHolder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        // Get current data.
        String name = list.get(position).getUname();
        String comment = list.get(position).getComment();
        String timestamp = list.get(position).getPtime();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String timeDate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        // Populate the textviews with data.
        holder.name.setText(name);
        holder.time.setText(timeDate);
        holder.comment.setText(comment);

    }

    /**
     * Required method for determining the size of the data set.
     * @return - Size of the data set.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder class that represents each row of data in the RecyclerView.
     */

    static class MyHolder extends RecyclerView.ViewHolder {

        // Member Variables for the TextViews
        TextView name, comment, time;
        ImageButton more;

        /**
         * Constructor for the MyHolder, used in onCreateViewHolder().
         * @param itemView - The root view of the row_comments.xml layout file.
         */
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views.
            more = itemView.findViewById(R.id.morebtn);
            name = itemView.findViewById(R.id.commentname);
            comment = itemView.findViewById(R.id.commenttext);
            time = itemView.findViewById(R.id.commenttime);
        }
    }
}