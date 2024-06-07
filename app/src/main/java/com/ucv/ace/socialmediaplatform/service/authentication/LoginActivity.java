package com.ucv.ace.socialmediaplatform.service.authentication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
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



@SuppressWarnings("deprecation")
public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;

    private Button mlogin;
    private TextView newAccount, recoverPassword;
    FirebaseUser currentUser;

    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle("Login into account");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        /* initialising the layout items */
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        newAccount = findViewById(R.id.needs_new_account);
        recoverPassword = findViewById(R.id.forget_password);
        mAuth = FirebaseAuth.getInstance();
        mlogin = findViewById(R.id.login_button);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mlogin.setOnClickListener(v -> {
            getUserCredentials();
        });

        newAccount.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        recoverPassword.setOnClickListener(v -> showRecoverPasswordDialog());
    }
    

    private void getUserCredentials() {
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();

        // if format of email doesn't matches return null
        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("Invalid Email Address!");
            email.setFocusable(true);

        } else {
            loginUser(mail, pass);
        }
    }

    /**
     * Recover Your Password using email.
     */
    @SuppressLint("SetTextI18n")
    private void showRecoverPasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Your Password");

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

        builder.setPositiveButton("Recover password", (dialog, which) -> {
            String mail = mailTextField.getText().toString().trim();
            beginRecovery(mail);//send a mail message on the mail to recover the password
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void beginRecovery(String mail) {
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        /**
         * send reset password email
         */
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
     *  Signing in user with the email and password written by the user.
     *  If it fails then we will be showing the error.
     *
     * @param mail - user email
     * @param password - user password
     */
    private void loginUser(String mail, String password) {
        loadingBar.setMessage("Logging In....");
        loadingBar.show();

        /**
         * sign in with email and password after authenticating
         */
        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                loadingBar.dismiss();
                FirebaseUser user = mAuth.getCurrentUser();

                if (Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser()) {

                    String email = Objects.requireNonNull(user).getEmail();
                    String userId = user.getUid();

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
