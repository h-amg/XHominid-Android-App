package com.main.android.activium;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ResetPassActivity extends AppCompatActivity {
    private static final String TAG = "ResetPassActivity";


    @BindView(R2.id.input_email_reset)
    EditText _emailText;
    @BindView(R2.id.btn_reset_pass)
    Button _resetPassButton;
    @BindView(R2.id.email_input_layout)
    TextInputLayout _email_input_layout;

    private CardView notice;
    final StitchAppClient client = Stitch.getDefaultAppClient();


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null).
        if (client.getAuth().isLoggedIn()) {
            //send user to login layout
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_email);
        ButterKnife.bind(this);

        notice = findViewById(R.id.noticeCard);


        _resetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideSoftKeyboard(ResetPassActivity.this);

                String email = _emailText.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("pseudoCache", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lastUserEmail", email);
                editor.apply();

                resetPass(email);
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


    public void resetPass(String email){

        UserPasswordAuthProviderClient emailPassClient = Stitch.getDefaultAppClient().getAuth().getProviderClient(
                UserPasswordAuthProviderClient.factory);

        emailPassClient.sendResetPasswordEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {
                        if (task.isSuccessful()) {
                            _resetPassButton.setVisibility(View.GONE);
                            _email_input_layout.setVisibility(View.GONE);
                            notice.setVisibility(View.VISIBLE);
                        }else{
                            Log.e("stitch", "Error sending password reset email:",
                                    task.getException());
                            Toast.makeText(ResetPassActivity.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            UserPasswordAuthProviderClient emailPassClient = client.getAuth().getProviderClient(UserPasswordAuthProviderClient.factory);
                            emailPassClient.resendConfirmationEmail(email);
                        }
                    }
                }
                );
    }
}
