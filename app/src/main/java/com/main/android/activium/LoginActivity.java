package com.main.android.activium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;

import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.core.auth.providers.facebook.FacebookCredential;
import com.mongodb.stitch.core.auth.providers.google.GoogleCredential;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;



public class LoginActivity extends AppCompatActivity implements SignupActivity.socialLogIn{
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R2.id.input_email)
    EditText _emailText;
    @BindView(R2.id.input_password)
    EditText _passwordText;
    @BindView(R2.id.btn_login)
    Button _loginButton;
    @BindView(R2.id.link_signup)
    TextView _signupLink;
    @BindView(R2.id.google_signin)
    Button _google_signin;
    @BindView(R2.id.facebook_sign_in)
    Button _facebook_sign_in;
    @BindView(R2.id.link_reset_pass)
    TextView linkResetPass;

    // firebase variables
    /*private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth mAuth;*/

    // view variables
    private ProgressDialog progressDialog;

    private UserPasswordCredential credential;
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private GoogleSignInClient googleSignInClient;
    final int RC_SIGN_IN = 7;  // activity request code
    private CallbackManager callbackManager;
    private RemoteMongoClient mongoClient;
    private SharedPreferences pseudoCache;
    private SharedPreferences chkPoint;



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null). //Mangoodb stitch
        if (stitchClient.getAuth().isLoggedIn()) {
            //send user to login layout
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // instantiating progress dialog
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");

        //initiate google sign in stitchClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // initializing facebook login
        _facebook_sign_in.setOnClickListener(this::facebooklogin);

        // google sign in button onclick listbner
        _google_signin.setOnClickListener(this::googleLogIn);

        //initialize collection
        mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory
                , "project/app_name");


        // initialized shared preferences and retrieved required data
        pseudoCache = getSharedPreferences("pseudoCache", MODE_PRIVATE);
        chkPoint = getSharedPreferences("chkPoint", MODE_PRIVATE);



        // login button listener
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideSoftKeyboard(LoginActivity.this);
                login();
            }
        });



        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });


        linkResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to reset pass activity
                Intent intent = new Intent(getApplicationContext(), ResetPassActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        // show progress dialog
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("pseudoCache", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastUserEmail", email);
        editor.putString("lastUserPass", password);
        editor.apply();


        //MongoDB user authentication
        credential = new UserPasswordCredential(email, password);
        stitchClient.getAuth().loginWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            /*Log.d("stitch", "Successfully logged in as user "
                                    + task.getResult().getId());*/
                            /*Toast.makeText(LoginActivity.this,
                                    "logged in Successfully",
                                    Toast.LENGTH_LONG).show();*/

                            new Thread(new Runnable() {
                                public void run() {
                                    // remove pseudoCached data
                                    removeTempData();
                                    // update setup check points
                                    updateChkPoint();
                                }
                            }).start();

                            progressDialog.dismiss();
                            onLoginSuccess();
                        } else {
                            String exceptMsg  = task.getException().getMessage();
                            boolean isFound = false;
                            try{
                                // check if email confirmation exception is thrown.
                                isFound = exceptMsg.contains("confirmation");
                            }catch(Exception e){
                                /*Log.e("stitch", "No confirmation error found: " + e);*/
                            }
                            if (isFound){
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "This email already" +
                                        " exists and requires confirmation",Toast.LENGTH_SHORT).show();
                                /*Log.d("stitch", "User confirmation exception found." +
                                        " sending user to confirmation activity");*/
                                onconfirmUser(email); // send user to email confirmation activity
                            }
                            Log.e("stitch", "Error logging in with email/password auth:",
                                    task.getException());
                            progressDialog.dismiss();
                            onLoginSuccess();// TODO:TEMPORARY WORKAROUND
                            Toast.makeText(LoginActivity.this, "Log in error, please" +
                                            " check your email and password",Toast.LENGTH_SHORT).show();
                            onLoginFailed();
                        }
                    }
                });
    }

    public static void hideSoftKeyboard(Activity activity) {
        try{
            InputMethodManager inputMethodManager = (InputMethodManager)
                    activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), 0);
        }catch(Exception e){
            /*Log.d(TAG, "Hide soft keyboard error: " + e);*/
        }
    }


    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }*/

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onconfirmUser(String email) {
        _loginButton.setEnabled(true);
        finish();
        Intent intent = new Intent(LoginActivity.this, EmailConfirmActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
    }


    public void onLoginFailed() {
        _loginButton.setEnabled(true);
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

        if (password.isEmpty() || password.length() < 6) {
            _passwordText.setError("should be more than 6 alphanumeric characters");
            valid = false;
            /*Log.e(TAG,"password length :" + password.length());*/
            /*Log.e(TAG,"Text box status :" + password.isEmpty());*/
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    /** Google login **/
    public void googleLogIn(final View ignored) {
        // show progress dialog
        progressDialog.show();
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // facebook callback
        if (requestCode != 7){
            try{
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }catch (Exception e){
                Log.e("facebook", "callback error: " + e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGooglSignInResult(task);
            return;
        }
    }

    private void handleGooglSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            final GoogleCredential googleCredential =
                    new GoogleCredential(account.getServerAuthCode());

            stitchClient.getAuth().loginWithCredential(googleCredential).addOnCompleteListener(
                    new OnCompleteListener<StitchUser>() {
                        @Override
                        public void onComplete(@NonNull final Task<StitchUser> task) {
                            if (task.isSuccessful()) {
                                // Do something here if the user logged in successfully.
                                /*Log.d("google", "Successfully logged in as user: "
                                        + task.getResult().getId());*/
                                /*Toast.makeText(LoginActivity.this,
                                        "Logged in Successfully",
                                        Toast.LENGTH_LONG).show();*/

                                new Thread(new Runnable() {
                                    public void run() {
                                        // remove pseudoCached data
                                        removeTempData();
                                        // update setup check points
                                        updateChkPoint();
                                    }
                                }).start();

                                progressDialog.dismiss();
                                onLoginSuccess();
                            } else {
                                Log.e(TAG, "Error logging in with Google", task.getException());
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this,
                                        "Log in error, make sure your email and password are" +
                                                " correct", Toast.LENGTH_SHORT).show();
                                onLoginFailed();
                            }
                        }
                    });
        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(LoginActivity.this,
                    "Log in error, make sure your email and password are" +
                            " correct", Toast.LENGTH_SHORT).show();
            onLoginFailed();
        }
    }

    // Facebook log in **/
    public void facebooklogin(final View ignored) {
        // show progress dialog
        progressDialog.show();
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        final FacebookCredential fbCredential =
                                new FacebookCredential(loginResult.getAccessToken().getToken());
                        Stitch.getDefaultAppClient().getAuth().loginWithCredential(fbCredential)
                                .addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<StitchUser> task) {
                                        if (task.isSuccessful()) {
                                            // Do something here
                                            /*Log.d("Facebook login", "Successfully logged in as user "
                                                    + task.getResult().getId());*/
                                            /*Toast.makeText(LoginActivity.this,
                                                    "Logged in Successfully",
                                                    Toast.LENGTH_LONG).show();*/

                                            new Thread(new Runnable() {
                                                public void run() {
                                                    // remove pseudoCached data
                                                    removeTempData();
                                                    // update setup check points
                                                    updateChkPoint();
                                                }
                                            }).start();

                                            progressDialog.dismiss();
                                            onLoginSuccess();
                                        } else {
                                            Log.e("Facebook login", "Not successful logging in with Facebook",
                                                    task.getException());
                                            Toast.makeText(LoginActivity.this,
                                                    "Log in error, make sure your email and password are" +
                                                            " correct", Toast.LENGTH_SHORT).show();
                                            /*onLoginFailed();
                                            progressDialog.dismiss();*/




                                            /*  TEMPORARY WORKARAOUND FACEBOOK LOGIN BUG:
                                            *  Not successful logging in with Facebook
                                            *  java.util.ConcurrentModificationException*/
                                            new Thread(new Runnable() {
                                                public void run() {
                                                    // remove pseudoCached data
                                                    removeTempData();
                                                    // update setup check points
                                                    updateChkPoint();
                                                }
                                            }).start();

                                            progressDialog.dismiss();
                                            onLoginSuccess();

                                        }
                                    }
                                } );

                    }
                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this,
                                "Facebook login Canceled",
                                Toast.LENGTH_LONG).show();
                        onLoginFailed();
                    }
                    @Override
                    public void onError(FacebookException error) {
                        Log.e(TAG, "Error logging in with Facebook",
                                error);
                        Toast.makeText(LoginActivity.this,
                                "Log in error, make sure your email and password are" +
                                        " correct", Toast.LENGTH_SHORT).show();
                        onLoginFailed();
                    }}
        );
        LoginManager.getInstance().logInWithReadPermissions(
                LoginActivity.this,
                Arrays.asList("public_profile")
        );
    }

    //  FB login from signup layout
    @Override
    public void facebooklogin() {
        facebooklogin(_facebook_sign_in);
    }

    //  Google login from signup layout
    @Override
    public void googleLogIn() {
        googleLogIn(_google_signin);
    }

    private void removeTempData(){
        String pass = pseudoCache.getString("lastUserPass", null);
        if (pass != null){
            // clear pseudoCached login data
            SharedPreferences.Editor editor = pseudoCache.edit();
            editor.remove("lastUserPass");
            editor.apply();
            /*Log.d("removeTempData", "removed pseudoCached data successfully!");*/
        }else{
            /*Log.d("removeTempData", "No pass value found.");*/
        }
    }

    private void updateChkPoint(){
        try{
            // clear chkPoint login data
            SharedPreferences.Editor chkPointEditor = chkPoint.edit();
            chkPointEditor.putBoolean("initDoc", true);
            chkPointEditor.apply();
            /*Log.e("updateChkPoint", "updated chkPoint data successfully!");*/
        }catch(Exception e){
            Log.e("updateChkPoint", "error removing chkPoint data: " + e);
        }
    }
}

