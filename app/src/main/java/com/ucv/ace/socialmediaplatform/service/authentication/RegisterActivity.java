package com.ucv.ace.socialmediaplatform.service.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.service.board.DashboardActivity;

import java.util.HashMap;
import java.util.Objects;

/**
 * The {@code RegisterActivity} handles user registration functionality.
 * Users can register using their email, password, and name, and their details
 * are stored in Firebase authentication and database.
 */

public class RegisterActivity extends AppCompatActivity {

    private EditText email, password, name;
    private Button mRegister;
    private TextView existAccount;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    /**
     * Called when the activity is first created. Initializes the UI components
     * and sets up Firebase authentication.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           being previously shut down, this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Setting up the ActionBar
        ActionBar actionBar = getSupportActionBar();

        Objects.requireNonNull(actionBar).setTitle("Create Your Account");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        // Initialize UI elements
        email = findViewById(R.id.register_email);
        name = findViewById(R.id.register_name);
        password = findViewById(R.id.register_password);
        mRegister = findViewById(R.id.register_button);
        existAccount = findViewById(R.id.homepage);

        // Firebase authentication setup
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Register");

        // Set up click listener for the register button
        mRegister.setOnClickListener(v -> {

            getUserCredential();
        });

        // Redirect to LoginActivity when "Already have an account" is clicked
        existAccount.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    /**
     * Retrieves user credentials (email, password, and name) from the input fields
     * and validates them before proceeding to register the user.
     */
    private void getUserCredential() {
        String mail = email.getText().toString().trim();
        String uname = name.getText().toString().trim();
        String pass = password.getText().toString().trim();

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("Invalid Email Address !");
            email.setFocusable(true);

        }
        // Validate password length
        else if (pass.length() < 6) {
            password.setError("Length Must be greater than 6 character !!");
            password.setFocusable(true);
        }
        // If inputs are valid, proceed with registration
        else {
            registerUser(mail, pass, uname);
        }
    }

    /**
     * Registers the user using the provided email, password, and username.
     * If registration is successful, user details are saved to the Firebase database,
     * and the user is redirected to the DashboardActivity.
     *
     * @param mail     User's email.
     * @param password User's password.
     * @param userName User's name.
     */
    private void registerUser(String mail, final String password, final String userName) {

        progressDialog.show();
        // Create a new user with the provided email and password in Firebase authentication
        mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                progressDialog.dismiss();

                // Get the registered user's details
                FirebaseUser user = mAuth.getCurrentUser();
                String email = Objects.requireNonNull(user).getEmail();
                String userID = user.getUid();

                // Create a HashMap to store the user's information
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("email", email);
                hashMap.put("uid", userID);
                hashMap.put("name", userName);
                hashMap.put("onlineStatus", "online");
                hashMap.put("typingTo", "noOne");
                hashMap.put("image", "");

                // Store the user information in Firebase database under the "Users" node
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("Users");

                reference.child(userID).setValue(hashMap);

                // Show a success message and navigate to the DashboardActivity
                Toast.makeText(RegisterActivity.this, "Registered User " + user.getEmail(), Toast.LENGTH_LONG).show();

                Intent mainIntent = new Intent(RegisterActivity.this, DashboardActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();

            } else {
                // Registration failed, show an error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            // Handle failure and show an error message
            progressDialog.dismiss();
            Toast.makeText(RegisterActivity.this, "Error Occurred", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Handles the navigation when the back button in the ActionBar is clicked.
     *
     * @return Returns true if the operation is successful.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
