package com.main.android.activium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;

import butterknife.ButterKnife;

import static com.mongodb.client.model.Filters.and;

public class EmailConfirmActivity extends AppCompatActivity {

    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private UserPasswordAuthProviderClient emailPassClient;
    private Button resned_btrn;
    private String email;
    private UserPasswordCredential credential;
    private RemoteMongoClient mongoClient;
    private SharedPreferences pseudoCache;
    private SharedPreferences chkPoint;

    @Override
    protected void onStart() {
        super.onStart();
        resned_btrn.setEnabled(true);
        // Check if user is signed in send to Maina Activity.
        if (stitchClient.getAuth().isLoggedIn()) {
            //send user to login layout
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "Please Confirm your email by clicking the link sent to" +
                        " your inbox", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_confrim);
        ButterKnife.bind(this);

        //initialize collection
        mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "project/app_name");

        // link resend button
        resned_btrn = findViewById(R.id.resned_btrn);

        // initialized shared preferences and retrieved required data
        pseudoCache = getSharedPreferences("pseudoCache", MODE_PRIVATE);
        chkPoint = getSharedPreferences("chkPoint", MODE_PRIVATE);
        email = pseudoCache.getString("lastUserEmail", null);
        /*Log.d("stitch", "Retrieved pseudoCache email: " + email);*/

        // initialize MongoDB emailpass stitchClient for user auth and confirmation
        emailPassClient = stitchClient.getAuth().getProviderClient(UserPasswordAuthProviderClient.factory);

        // handle password confirmation when activity is launched
        String token;
        String tokenId;
        Uri appLinkUri = getIntent().getData();
        if (appLinkUri != null){
            token = appLinkUri.getQueryParameter("token");
            tokenId = appLinkUri.getQueryParameter("tokenId");
            if (token != null && tokenId != null){
                confirmUser(token, tokenId);
            }
        }

        resned_btrn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailPassClient.resendConfirmationEmail(email);
                Toast.makeText(EmailConfirmActivity.this,
                        "Confirmation email sent!", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void confirmUser(String token, String tokenId) {
        resned_btrn.setEnabled(false);

        // Initialize progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(EmailConfirmActivity.this,
                R.style.AppTheme_Dark_Dialog);

        // instantiating progress dialog
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Verifying...");
        progressDialog.show();

        emailPassClient.confirmUser(token, tokenId)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailConfirmActivity.this,
                                    "Email confirmed!", Toast.LENGTH_SHORT).show();
                            /*Log.d("stitch", "Successfully confirmed user");*/
                            progressDialog.dismiss();

                            logUserIn(email);
                        } else {
                            Log.e("stitch", "Error confirming user: ",
                                    task.getException());
                            /*String exception  = task.getException().getMessage();*/
                            Toast.makeText(EmailConfirmActivity.this, "Error confirming" +
                                            " your email," + " please try again",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            onConfirmFailed();
                        }
                    }
                });
    }

    public  void logUserIn(String email){
        /*Log.d("logUserIn", "login triggered");*/
        final ProgressDialog progressDialog = new ProgressDialog(EmailConfirmActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        String password = pseudoCache.getString("lastUserPass", null);

        if(email != null && password != null) {
            credential = new UserPasswordCredential(email, password);
            stitchClient.getAuth().loginWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                        @Override
                        public void onComplete(@androidx.annotation.NonNull final Task<StitchUser> task) {
                            if (task.isSuccessful()) {
                                /*Log.d("logUserIn", "Successfully logged in as user "
                                        + task.getResult().getId());*/
                                new Thread(new Runnable() {
                                    public void run() {
                                        // remove pseudoCached data
                                        removeTempData();
                                        // update setup check points
                                        updateChkPoint();
                                    }
                                }).start();
                                //send user to main activity
                                onLoginSuccess();

                            } else {
                                /*String exceptMsg = task.getException().getMessage();*/
                                Log.e("logUserIn", "Error logging in with email/password auth:",
                                        task.getException());
                                progressDialog.dismiss();
                                Toast.makeText(EmailConfirmActivity.this,
                                        "Log in error, make sure your email and password are" +
                                                " correct", Toast.LENGTH_SHORT).show();
                                // clear pseudoCached login data
                                SharedPreferences.Editor editor = pseudoCache.edit();
                                editor.remove("lastUserPass");
                                editor.apply();

                                onFailure();
                            }
                        }
                    });
        }else{
            Log.e("logUserIn", "Error logging user in: password/email = null");
            Toast.makeText(EmailConfirmActivity.this,
                    "Log in error, make sure your email and password are" +
                            " correct", Toast.LENGTH_SHORT).show();
            onFailure();
        }
    }

    public void onLoginSuccess() {
        resned_btrn.setEnabled(true);
        finish();
        startActivity(new Intent(EmailConfirmActivity.this,MainActivity.class));
    }

    public void onFailure(){
        /*Log.d("onFailure", "triggered");*/
        new Thread(new Runnable() {
            public void run() {
                // remove pseudoCached data
                removeTempData();
                // update setup check points
                updateChkPoint();
            }
        }).start();
        resned_btrn.setEnabled(true);
        finish();
        startActivity(new Intent(EmailConfirmActivity.this,LoginActivity.class));
    }

    public void onConfirmFailed() {
        resned_btrn.setEnabled(true);
        setResult(RESULT_CANCELED, null);
        finish();
        startActivity(new Intent(EmailConfirmActivity.this, LoginActivity.class));
    }

    private void removeTempData(){
        try{
            // clear pseudoCached login data
            SharedPreferences.Editor editor = pseudoCache.edit();
            editor.remove("lastUserPass");
            editor.apply();
            /*Log.d("removeTempData", "removed pseudoCached data successfully!");*/
        }catch(Exception e){
            Log.e("removeTempData", "error removing pseudoCached data: " + e);
        }
    }

    private void updateChkPoint(){
        try{
            // clear pseudoCached login data
            SharedPreferences.Editor chkPointEditor = chkPoint.edit();
            chkPointEditor.putBoolean("initDoc", true);
            chkPointEditor.apply();
            /*Log.d("updateChkPoint", "updated chkPoint data successfully!");*/
        }catch(Exception e){
            Log.e("updateChkPoint", "error removing chkPoint data: " + e);
        }
    }
}
