package com.ucv.ace.socialmediaplatform.service.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ucv.ace.socialmediaplatform.R;

/**
 * This activity handles the display of posts that the user has liked.
 * It inflates the layout for the liked posts and initializes any necessary components.
 */
public class PostLikedActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created. This is where most initialization should go, such as setting the content view.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the "activity_post_liked" layout.
        setContentView(R.layout.activity_post_liked);
    }
}
