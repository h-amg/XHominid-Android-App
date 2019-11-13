package com.main.android.activium;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.main.android.activium.DataClasses.Settings;
import com.main.android.activium.dialogs.FeedbackDialog;
import com.main.android.activium.dialogs.SettingsDialog;
import com.main.android.activium.dialogs.UpdateDetailsDialog;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

public class SettingsActivity extends AppCompatActivity
        implements SettingsDialog.DialogLisstner,
        NavigationView.OnNavigationItemSelectedListener, FeedbackDialog.InterFeedBack,
        UpdateDetailsDialog.UpdateDetailsInterface{
    private static final String TAG = "Settings Activity";

    private static final int logperiod = 100;
    private static final int dietStatperiod = 200;
    private int identifier;
    private Button mMc_rem_btn;
    private Button mDt_rem_btn;


    private Intent intent;
    // Intent for app play store link sharing
    private Intent shareIntent;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Switch mlog_remind_swt;
    private Switch mdt_status_swt;
    /*private Switch maud_resp_swt;*/
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;


    // settings document
    private Settings settingsDocument;


    // MongoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoCollection settingsCollection;
    private RemoteMongoClient mongoClient;

    // update_details drawer click check
    private boolean isUpdateDialog;

    /**User authentication ON APP launch*/
    // check if the user is logged in when the app is launched
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null).
        if (!stitchClient.getAuth().isLoggedIn()) {
            finish();
            //send user to login layout
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //declaring local variables
        Toolbar toolBar;

        // initialized shared preferences and retrieved required data
        settings = getSharedPreferences("settings", MODE_PRIVATE);
        settingsEditor = settings.edit();

        //Link switch views
        mlog_remind_swt = findViewById(R.id.log_remind_swt);
        mdt_status_swt = findViewById(R.id.dt_status_swt);
        /*maud_resp_swt = findViewById(R.id.aud_resp_swt);*/
        toolBar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.settings_layout);
        navigationView = findViewById(R.id.nav_view_settings);
        mMc_rem_btn = findViewById(R.id.log_mc_notset);
        mDt_rem_btn = findViewById(R.id.dt_st_notset);

        //initialize collection
        mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "project/app_name");
        settingsCollection = mongoClient.getDatabase("database_name")
                .getCollection("collection_name");

        // update ui
        updateUI();

        /**setup toolbar and drawer*/
        toolBar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolBar);
        // menu hamburger button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_black_36);
            getSupportActionBar().setTitle("Settings");
        }
        //set up drawer_menu Navigation listener
        setNavigationViewListener();
        // Drawer state change listener
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {}
            @Override public void onDrawerOpened(View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {}

            @Override
            public void onDrawerClosed(View drawerView) {
                //Set your new fragment here
                if (intent != null && intent.resolveActivity(getPackageManager()) != null && shareIntent == null) {
                    finish();
                    startActivity(intent);
                    intent = null;
                }else if (intent != null && intent.resolveActivity(getPackageManager()) != null && shareIntent != null){
                    finish();
                    startActivity(shareIntent);
                    shareIntent = null;
                }

                if(isUpdateDialog){
                    // show update details dialog
                    UpdateDetailsDialog updateDetailsDialog = new UpdateDetailsDialog();
                    updateDetailsDialog.show(getSupportFragmentManager(), "Update Details");
                    isUpdateDialog = false;
                }
            }
        });


        // Log macros reminder switch change listener
        mlog_remind_swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    updateMacLogState(true);
                    setSync(false);  // indicate remote DB not synced remote
                    //TODO: update remote
                    mMc_rem_btn.setClickable(true);
                    mMc_rem_btn.setBackground(getDrawable(R.drawable.setting_btn_clickable));
                    Toast.makeText(SettingsActivity.this,
                            "Macro logging reminders ENABLED", Toast.LENGTH_LONG).show();
                }else{
                    updateMacLogState(false);
                    setSync(false);  // indicate remote DB not synced remote
                    //TODO: update remote
                    mMc_rem_btn.setClickable(false);
                    mMc_rem_btn.setBackground(getDrawable(R.drawable.setting_btn_non_clickable));
                    Toast.makeText(SettingsActivity.this,
                            "Macro logging reminders DISABLED", Toast.LENGTH_LONG).show();
                }
                /*Log.d("TAG", "Macro logging reminder switch: " + isChecked);*/
            }
        });
        //diet stats reminder switch listener
        mdt_status_swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    updatedtstate(true);
                    setSync(false);  // indicate remote DB not synced remote
                    //TODO: update remote
                    mDt_rem_btn.setClickable(true);
                    mDt_rem_btn.setBackground(getDrawable(R.drawable.setting_btn_clickable));
                    Toast.makeText(SettingsActivity.this,
                            "Diet stats reminders ENABLED", Toast.LENGTH_LONG).show();
                }else{
                    updatedtstate(false);
                    setSync(false);  // indicate remote DB not synced remote
                    //TODO: update remote
                    mDt_rem_btn.setClickable(false);
                    mDt_rem_btn.setBackground(getDrawable(R.drawable.setting_btn_non_clickable));
                    Toast.makeText(SettingsActivity.this,
                            "Diet stats reminders DISABLED", Toast.LENGTH_LONG).show();
                }
                /*Log.d("TAG", "Diet stats reminder switch: " + isChecked);*/
            }
        });
        //Audible response switch listener
        /*maud_resp_swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    updateaudioState(true);
                    setSync(false);  // indicate remote DB not synced remote
                    //TODO: update remote
                    Toast.makeText(SettingsActivity.this,
                            "Audible response reminders ENABLED", Toast.LENGTH_LONG).show();
                }else{
                    updateaudioState(false);
                    setSync(false);  // indicate remote DB not synced remote
                    //TODO: update remote
                    Toast.makeText(SettingsActivity.this,
                            "Audible response reminders DISABLED", Toast.LENGTH_LONG).show();
                }
                *//*Log.d("TAG", "Audible response reminder switch: " + isChecked);*//*
            }
        });*/

        mMc_rem_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogperiodDialog();
                identifier = logperiod;
            }
        });

        // update diet stats reminder frequency button
        mDt_rem_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogperiodDialog();
                identifier = dietStatperiod;
            }
        });


        // logout button
        Button btnLogout = findViewById(R.id.logout);
        // logout button listener
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stitchClient.getAuth().logout();  //logs the user out
                finish();
                startActivity(new Intent(SettingsActivity.this,LoginActivity.class));
            }
        });


    }

    public void showLogperiodDialog() {
       SettingsDialog logPeriodDialog = new SettingsDialog();
       logPeriodDialog.show(getSupportFragmentManager(), "log period dialog");
   }

    @Override
    public void updateLogPeriod(int period) {
        String btnLabel = period + " " + "Hours";
        // update setting data
        updateMacLogfrq(period);
        setSync(false);  // indicate remote DB not synced remote
        //TODO: update remote
        mMc_rem_btn.setText(btnLabel);
        Toast.makeText(this, "Log period settings updated", Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateDtStatPeriod(int period) {
        String btnLabel = period + " " + "Hours";
        updatedtfrq(period);
        setSync(false);  // indicate remote DB not synced remote
        //TODO: update remote
        mDt_rem_btn.setText(btnLabel);
        Toast.makeText(this, "Diet stats Settings updated", Toast.LENGTH_LONG).show();
    }


    @Override
    public int dialogIdentifier() {
        return identifier;
    }


    private void setNavigationViewListener() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        /*int id = item.getItemId();*/

        switch (item.getItemId()) {
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_meal_plans:
                intent = new Intent(this, MealPlansActivity.class);
                break;
            case R.id.nav_shopping_list:
                intent = new Intent(this, ShoppingListActivity.class);
                break;
            case R.id.nav_meassages:
                intent = new Intent(this, MessagesActivity.class);
                break;
            case R.id.nav_counselling:
                intent = new Intent(this, ConsultationActivity.class);
                break;
            /*case R.id.nav_ml_record:
                intent = new Intent(this, MealsRecordActivity.class);
                break;*/
            /*case R.id.nav_dt_stats:
                intent = new Intent(this, DietStatsActivity.class);
                break;*/
            case R.id.nav_update_details:
                isUpdateDialog = true;
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_share:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Activium");
                String shareMessage= "\nHey! you might wanna try this out.\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                String title = getResources().getString(R.string.chooser_title);
                shareIntent = Intent.createChooser(intent, title);
                break;
            case R.id.nav_feedback:
                showFeedbackDialog();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                hideSoftKeyboard(this);
                drawer.openDrawer(Gravity.LEFT);
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void updateMacLogState(boolean macro_logging_state){
        try {
            // update settings data
            settingsEditor.putBoolean("macro_logging_state", macro_logging_state);
            settingsEditor.apply();
        }catch(Exception e){
            Log.e("updateMacLogState", "error updating settings data: " + e);
        }
    }

    private void updateMacLogfrq(int macro_logging_int){
        try {
            // update settings data
            settingsEditor.putInt("macro_logging_int", macro_logging_int);
            settingsEditor.apply();
        }catch(Exception e){
            Log.e("updateMacLogint", "error updating settings data: " + e);
        }
    }

    private void updatedtstate(boolean diet_status_state){
        try {
            // update settings data
            settingsEditor.putBoolean("diet_status_state", diet_status_state);
            settingsEditor.apply();
        }catch(Exception e){
            Log.e("updatedtstate", "error updating settings data: " + e);
        }
    }

    private void updatedtfrq(int diet_status_int){
        try {
            // update settings data
            settingsEditor.putInt("diet_status_int", diet_status_int);
            settingsEditor.apply();
        }catch(Exception e){
            Log.e("updatedtfrq", "error updating settings data: " + e);
        }
    }

    private void updateaudioState(boolean audio){
        try {
            // clear settings login data
            settingsEditor.putBoolean("audio", audio);
            settingsEditor.apply();
        }catch(Exception e){
            Log.e("updateaudioState", "error removing settings data: " + e);
        }
    }

    private void setSync (boolean syncState){
        try {
            // update settings data
            settingsEditor.putBoolean("syncState", syncState);
            settingsEditor.apply();
        }catch(Exception e){
            Log.e("setSync", "error updating settings data: " + e);
        }
    }

    private void updateUI(){
        boolean macLogState = settings.getBoolean("macro_logging_state", false);
        int macLogInt = settings.getInt("macro_logging_int", 0);
        boolean dtState = settings.getBoolean("diet_status_state", false);
        int dtInt = settings.getInt("diet_status_int", 0);
        /*boolean audio = settings.getBoolean("audio", false);*/

        mlog_remind_swt.setChecked(macLogState);
        mMc_rem_btn.setText(String.valueOf(macLogInt));
        mdt_status_swt.setChecked(dtState);
        mDt_rem_btn.setText(String.valueOf(dtInt));
        /*maud_resp_swt.setChecked(audio);*/
    }

    public void showFeedbackDialog() {
        FeedbackDialog feedBackDialog = new FeedbackDialog();
        feedBackDialog.show(getSupportFragmentManager(), "feedBack");
    }

    @Override
    public void triggerSuccessToast() {
        Toast.makeText(SettingsActivity.this, "FeedBack Sent. Thank you",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFailureToast() {
        Toast.makeText(SettingsActivity.this, "Failed sending feedback!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboard() {
        hideSoftKeyboard(SettingsActivity.this);
    }

    @Override
    public void detailsSuccessUpdate() {
        Toast.makeText(SettingsActivity.this, "Details updated successfully",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void detailsFailureUpdate() {
        Toast.makeText(SettingsActivity.this, "Failed updating details!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboardUpdate() {
        hideSoftKeyboard(SettingsActivity.this);
    }
}