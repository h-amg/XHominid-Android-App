package com.main.android.activium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.facebook.FacebookCredential;
import com.mongodb.stitch.core.auth.providers.google.GoogleCredential;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";


    @BindView(R2.id.input_email)
    EditText _emailText;
    @BindView(R2.id.input_password)
    EditText _passwordText;
    @BindView(R2.id.btn_signup)
    Button _signupButton;
    @BindView(R2.id.link_login)
    TextView _loginLink;

    //firebase variables
    /*private FirebaseAuth mAuth;*/
    /*private FirebaseFirestore db;*/
    /*private FirebaseUser user;*/
    // MongoDB variables

    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private UserPasswordAuthProviderClient emailPassClient;
    private Intent intent;
    private ProgressDialog progressDialog;

    private GoogleSignInClient googleSignInClient;
    final int RC_SIGN_IN = 7;  // activity request code
    private CallbackManager callbackManager;
    private RemoteMongoClient mongoClient;
    private Boolean isFound;
    private socialLogIn socialLogIn;


    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in send to Maina Activity.
        if (stitchClient.getAuth().isLoggedIn()) {
            //send user to login layout
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        // initialize stitch emailPass stitchClient for user auth and confirmation
        emailPassClient = stitchClient.getAuth().getProviderClient(UserPasswordAuthProviderClient.factory);

        //initiate google sign in stitchClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        /*// initiate social login interface
        try {
            socialLogIn = (socialLogIn) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.toString() +
                    "must implement DialogListener");
        }*/

        //initialize collection
        mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "project/app_name");

        /*remoteClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        usersCollection = remoteClient.getDatabase("store").getCollection("users");*/

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");


        /*// handle password confirmation when activity is launched
        Intent appLinkIntent = getIntent();
        Uri appLinkUri = appLinkIntent.getData();
        //Log.d("Stitch", "Uri data: " + appLinkUri.toString());
        try{
            confirmUser(appLinkUri);
        }catch (Exception e){
            //Log.d("Stitch", "No email confirmation intent found: " + e);
        }*/




        // Initialize Firebase Auth
        /*mAuth = FirebaseAuth.getInstance();*/

        // Access a Cloud Firestore instance from your Activity
       /* db = FirebaseFirestore.getInstance();*/

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(SignupActivity.this);
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to the Login activity
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void signup() {

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("pseudoCache", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastUserEmail", email);
        editor.putString("lastUserPass", password);
        editor.apply();

        // TODO: Implement your own signup logic here.
        emailPassClient.registerWithEmail(email, password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            // send the user to confirmation layout
                            onConfirm(email);
                        } else {
                            Log.e("stitch", "Error registering new user:",
                                    task.getException());
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(SignupActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            _signupButton.setEnabled(true);

                            // clear pseudoCached login data
                            editor.remove("lastUserPass");
                            editor.apply();
                        }
                    }
                }
                );
    }

    public static void hideSoftKeyboard(Activity activity) {
        try{
            InputMethodManager inputMethodManager = (InputMethodManager)
                    activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), 0);
        }catch(Exception e){
            Log.e(TAG, "Hide soft keyboard error: " + e);
        }
    }

    public void onConfirm(String email) {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        // send the user to email confirmation layout
        Intent intent = new Intent(SignupActivity.this, EmailConfirmActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }


    public void onSignupFailed() {
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            _passwordText.setError("should be more than 6 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public interface socialLogIn{
        void facebooklogin();
        void googleLogIn();
    }

    private void goToSignup(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}