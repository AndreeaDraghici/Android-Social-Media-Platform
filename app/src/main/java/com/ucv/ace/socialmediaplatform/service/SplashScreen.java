package com.ucv.ace.socialmediaplatform.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.service.authentication.LoginActivity;
import com.ucv.ace.socialmediaplatform.service.board.DashboardActivity;

/**
 * Created by LittleDuck on 28.03.2023
 * Name of project: SocialMediaApplication
 */

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    /**
     * Here we are checking that if the user is null then go to LoginActivity.
     * Else move to DashboardActivity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        new Handler().postDelayed(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent mainIntent = new Intent(SplashScreen.this, DashboardActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            }
        }, 1000);
    }
}
