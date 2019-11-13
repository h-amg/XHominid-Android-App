package com.main.android.activium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewPassActivity extends AppCompatActivity {
    private static final String TAG = "NewPassActivity";

    @BindView(R2.id.input_password)
    EditText _passText;
    @BindView(R2.id.btn_reset)
    Button resetPassBtn;


    private UserPasswordCredential credential;
    final StitchAppClient client = Stitch.getDefaultAppClient();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);
        ButterKnife.bind(this);

        // get uir passed on as intent from the pass reset link
        Uri uri = getIntent().getData();

        resetPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassBtn.setEnabled(false);
                String password = _passText.getText().toString();
                hideSoftKeyboard(NewPassActivity.this);
                handlePasswordReset(password, uri);
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
            Log.e(TAG, "Hide soft keyboard error: " + e);
        }
    }

    public void handlePasswordReset(String password, Uri uri) {

        // instantiating progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(NewPassActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Updating new password...");
        progressDialog.show();

        String token = uri.getQueryParameter("token");
        String tokenId = uri.getQueryParameter("tokenId");
        String newPassword = password;

        UserPasswordAuthProviderClient emailPassClient = Stitch.getDefaultAppClient().getAuth().getProviderClient(
                UserPasswordAuthProviderClient.factory
        );

        emailPassClient
                .resetPassword(token, tokenId, newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(NewPassActivity.this,
                                    "Password reset succefully", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            logUserIn(password);
                        } else {
                            Log.e("stitch", "Error resetting user's password:",
                                    task.getException());
                            Toast.makeText(NewPassActivity.this,
                                    "Password reset failed", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            onFailure();
                        }
                    }
                });
    }

    public void onFailure(){
        resetPassBtn.setEnabled(true);
        finish();
        startActivity(new Intent(NewPassActivity.this,LoginActivity.class));
    }

    public  void logUserIn(String password){

        final ProgressDialog progressDialog = new ProgressDialog(NewPassActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        SharedPreferences sharedPref = getSharedPreferences("pseudoCache", MODE_PRIVATE);
        String email = sharedPref.getString("lastUserEmail", null);

        credential = new UserPasswordCredential(email, password);
        client.getAuth().loginWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            // clear pseudoCached login data
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.remove("lastUserPass");
                            editor.apply();

                            onLoginSuccess();
                        } else {
                            String exceptMsg  = task.getException().getMessage();
                            // check if email confirmation exception is thrown.
                            boolean isFound = false;
                            if (exceptMsg != null) {
                                isFound = exceptMsg.contains("confirmation");
                            }
                            if (isFound){
                                progressDialog.dismiss();
                                Toast.makeText(NewPassActivity.this, exceptMsg
                                        ,Toast.LENGTH_SHORT).show();
                                /*Log.d("stitch", "User confirmation exception found." +
                                        " sending user to confirmation activity");*/
                                onconfirmUser(email); // send user to email confirmation activity
                            }
                            Log.e("stitch", "Error logging in with email/password auth:",
                                    task.getException());
                            progressDialog.dismiss();
                            Toast.makeText(NewPassActivity.this, exceptMsg
                                    ,Toast.LENGTH_SHORT).show();

                            // clear pseudoCached login data
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.remove("lastUserPass");
                            editor.apply();

                            onFailure();
                        }
                    }
                });
    }

    public void onLoginSuccess() {
        resetPassBtn.setEnabled(true);
        finish();
        startActivity(new Intent(NewPassActivity.this,MainActivity.class));
    }

    public void onconfirmUser(String email) {
        resetPassBtn.setEnabled(true);
        finish();
        Intent intent = new Intent(NewPassActivity.this, EmailConfirmActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

}
