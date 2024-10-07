package com.ucv.ace.socialmediaplatform.service.authentication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
 * This activity handles user login functionality. Users can log in with their email
 * and password, create a new account, or recover their password if they forget it.
 * It interacts with Firebase for authentication and database operations.
 */

@SuppressWarnings("deprecation")
public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;

    private Button mlogin;
    private TextView newAccount, recoverPassword;
    FirebaseUser currentUser;
    private CheckBox rememberPasswordCheckBox;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;

    // Constant for SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";


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
        setContentView(R.layout.activity_login);

        // Initialize layout components
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        mlogin = findViewById(R.id.login_button);
        newAccount = findViewById(R.id.needs_new_account);
        recoverPassword = findViewById(R.id.forgot_password);
        rememberPasswordCheckBox = findViewById(R.id.remember_password);  // Initialize CheckBox

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        loadingBar = new ProgressDialog(this);

        // Check for saved login credentials
        checkSavedCredentials();

        // Set listener for login button
        mlogin.setOnClickListener(v -> getUserCredentials());

        // Redirect to RegisterActivity
        newAccount.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Password recovery
        recoverPassword.setOnClickListener(v -> showRecoverPasswordDialog());
    }

    // Check for saved credentials and pre-populate fields
    private void checkSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);

        if (rememberMe) {
            String savedEmail = sharedPreferences.getString("email", "");
            String savedPassword = sharedPreferences.getString("password", "");

            email.setText(savedEmail);
            password.setText(savedPassword);
            rememberPasswordCheckBox.setChecked(true);  // Set CheckBox to checked
        }
    }

    /**
     * Retrieves user credentials from the email and password fields
     * and calls the login method.
     */
    private void getUserCredentials() {
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("Invalid Email Address!");
            email.setFocusable(true);
        } else {
            // If "Remember Password" is checked, save credentials
            if (rememberPasswordCheckBox.isChecked()) {
                saveCredentials(mail, pass);
            } else {
                clearSavedCredentials();
            }

            // Proceed with login
            loginUser(mail, pass);
        }
    }

    // Save login credentials in SharedPreferences
    private void saveCredentials(String mail, String pass) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", mail);
        editor.putString("password", pass);
        editor.putBoolean("rememberMe", true);
        editor.apply();
    }

    // Clear saved login credentials if "Remember Password" is unchecked
    private void clearSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Clear all saved data
        editor.apply();
    }

    /**
     * Displays a dialog that allows the user to recover their password
     * by providing their registered email.
     */
    @SuppressLint("SetTextI18n")
    private void showRecoverPasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Your Password");

        // Set up a layout for entering the recovery email
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        final EditText mailTextField = new EditText(this);//write your registered email
        mailTextField.setHint("Add Your Email");
        mailTextField.setBackgroundColor(getResources().getColor(android.R.color.white));
        mailTextField.setMinEms(20);
        mailTextField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        linearLayout.addView(mailTextField);
        linearLayout.setPadding(15, 15, 25, 15);
        builder.setView(linearLayout);

        // Set dialog buttons for recovering the password or canceling
        builder.setPositiveButton("Recover password", (dialog, which) -> {
            String mail = mailTextField.getText().toString().trim();
            beginRecovery(mail);//send a mail message on the mail to recover the password
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    /**
     * Starts the password recovery process by sending a recovery email.
     *
     * @param mail The user's email where the recovery instructions will be sent.
     */
    private void beginRecovery(String mail) {
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        // Send reset password email using Firebase authentication
        mAuth.sendPasswordResetEmail(mail).addOnCompleteListener(task -> {
            loadingBar.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Done sent.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "Error Occurred", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            loadingBar.dismiss();
            Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_LONG).show();
        });
    }


    /**
     * Signs in the user with the provided email and password using Firebase authentication.
     * If this is a new user, their information will be added to the Firebase database.
     *
     * @param mail     The user's email address.
     * @param password The user's password.
     */

    private void loginUser(String mail, String password) {
        loadingBar.setMessage("Logging In....");
        loadingBar.show();

        // Authenticate with Firebase using the provided email and password
        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                loadingBar.dismiss();
                FirebaseUser user = mAuth.getCurrentUser();

                // Check if the user is logging in for the first time (new user)
                if (Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser()) {

                    String email = Objects.requireNonNull(user).getEmail();
                    String userId = user.getUid();

                    // Set up user data
                    HashMap<Object, String> hashMapModel = new HashMap<>();
                    hashMapModel.put("email", email);
                    hashMapModel.put("uid", userId);
                    hashMapModel.put("name", "");
                    hashMapModel.put("onlineStatus", "online");
                    hashMapModel.put("typingTo", "noOne");
                    hashMapModel.put("phone", "");
                    hashMapModel.put("image", "");
                    hashMapModel.put("cover", "");

                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                    // store the value in Database in "Users" Node
                    DatabaseReference reference = database.getReference("Users");

                    // storing the value in Firebase
                    reference.child(userId).setValue(hashMapModel);
                }

                Toast.makeText(LoginActivity.this, "Login User " + Objects.requireNonNull(user).getEmail() + " with success!", Toast.LENGTH_LONG).show();
                Intent mainIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            } else {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            loadingBar.dismiss();
            Toast.makeText(LoginActivity.this, "Error Occurred", Toast.LENGTH_LONG).show();
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
