package com.ucv.ace.socialmediaplatform.service.authentication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ucv.ace.socialmediaplatform.R;
import com.ucv.ace.socialmediaplatform.service.board.DashboardActivity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Objects;

/**
 * The LoginActivity class handles user login functionality, including
 * sign-in with email and password, Google Sign-In, and password recovery.
 * This activity interacts with Firebase for authentication and database
 * operations.
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

    // Google Sign-In
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private Button googleLoginButton;

    // Constant for SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";


    /**
     * Called when the activity is first created. This method initializes
     * the UI components, Firebase authentication, and Google Sign-In client.
     * It also checks for saved credentials and sets up button listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized
     *                           after being shut down, this Bundle contains
     *                           the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Setting up the ActionBar
        ActionBar actionBar = getSupportActionBar();

        Objects.requireNonNull(actionBar).setTitle("Login With Your Account");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize layout components
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        mlogin = findViewById(R.id.login_button);
        newAccount = findViewById(R.id.needs_new_account);
        recoverPassword = findViewById(R.id.forgot_password);
        rememberPasswordCheckBox = findViewById(R.id.remember_password);  // Initialize CheckBox
        googleLoginButton = findViewById(R.id.google_login_button);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        loadingBar = new ProgressDialog(this);

        // Google Sign-In configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for saved login credentials
        checkSavedCredentials();

        // Set listener for login button
        mlogin.setOnClickListener(v -> getUserCredentials());

        // Redirect to RegisterActivity
        newAccount.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Password recovery
        recoverPassword.setOnClickListener(v -> showRecoverPasswordDialog());

        // Set listener for Google login button
        googleLoginButton.setOnClickListener(v -> signInWithGoogle());
    }

    /**
     * Initiates the Google Sign-In process.
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handles the result of the Google Sign-In intent. If the sign-in
     * is successful, it proceeds with Firebase authentication.
     *
     * @param requestCode The request code passed to startActivityForResult().
     * @param resultCode  The result code returned by the child activity.
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign-In failed
                Log.w("LoginActivity", "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Authenticate the user with Firebase using the Google Sign-In token.
     *
     * @param idToken The ID token returned from Google Sign-In.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();

                if (firebaseUser == null) return;

                String uid = firebaseUser.getUid();
                String email = firebaseUser.getEmail();
                String name = firebaseUser.getDisplayName();
                String image = String.valueOf(firebaseUser.getPhotoUrl());

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("uid", uid);
                        userData.put("name", name != null ? name : "");
                        userData.put("image", image);
                        userData.put("phone", "");
                        userData.put("onlineStatus", "online");
                        userData.put("typingTo", "noOne");

                        if (!snapshot.exists()) {
                            // First login → add cover as empty
                            userData.put("cover", "");
                            userRef.setValue(userData);
                        } else {
                            // Don't override existing "cover"
                            userRef.updateChildren(userData);
                        }

                        // Launch main activity after successful DB update
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Firebase error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks for saved login credentials in SharedPreferences and
     * pre-populates the email and password fields if credentials exist.
     */
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

    /**
     * Saves the login credentials (email and password) in SharedPreferences
     * if the user has selected the "Remember Password" option.
     *
     * @param mail The user's email address.
     * @param pass The user's password.
     */
    private void saveCredentials(String mail, String pass) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", mail);
        editor.putString("password", pass);
        editor.putBoolean("rememberMe", true);
        editor.apply();
    }

    /**
     * Clears saved login credentials from SharedPreferences if the user
     * has unchecked the "Remember Password" option.
     */
    private void clearSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Clear all saved data
        editor.apply();
    }

    /**
     * Displays a dialog to recover the user's password by providing their
     * registered email address. A password reset email will be sent to
     * the provided email.
     */
    @SuppressLint("SetTextI18n")
    private void showRecoverPasswordDialog() {
        // Creează dialog cu stil custom
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Recover Your Password");

        // Setup layout principal
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);
        layout.setBackgroundResource(R.drawable.dialog_background);

        // Email input
        EditText emailInput = new EditText(this);
        emailInput.setHint("Add your email");
        emailInput.setHintTextColor(getResources().getColor(R.color.neutral));
        emailInput.setTextColor(getResources().getColor(R.color.colorBlack));
        emailInput.setBackgroundResource(R.drawable.edittext_background);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, 0, 0);
        emailInput.setCompoundDrawablePadding(24);
        emailInput.setPadding(40, 30, 40, 30);

        layout.addView(emailInput);
        builder.setView(layout);

        // Butoane
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Recover Password", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            beginRecovery(email);
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Stil butoane
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorgray01));
    }


    /**
     * Initiates the password recovery process by sending a password reset
     * email to the user's registered email address.
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
     * Signs in the user with the provided email and password using Firebase
     * authentication. If this is a new user, their information will be
     * added to the Firebase database.
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
                if (user != null) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

                    HashMap<String, Object> userData = new HashMap<>();
                    userData.put("email", user.getEmail());
                    userData.put("uid", user.getUid());

                    String currentName = user.getDisplayName() != null ? user.getDisplayName() : null;
                    if (currentName != null && !currentName.isEmpty()) {
                        userData.put("name", currentName);
                    }

                    String currentPhotoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
                    if (currentPhotoUrl != null && !currentPhotoUrl.isEmpty()) {
                        userData.put("image", currentPhotoUrl);
                    }
                    userData.put("onlineStatus", "online");
                    userData.put("typingTo", "noOne");

                    userRef.updateChildren(userData);
                }
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
