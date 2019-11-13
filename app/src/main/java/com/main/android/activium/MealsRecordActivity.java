package com.main.android.activium;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.navigation.NavigationView;

/*import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;*/

import com.main.android.activium.Adapters.DayMealsAdapter;
import com.main.android.activium.Adapters.RecordsdaysAdapter;
import com.main.android.activium.DataClasses.LoggedMeal;
import com.main.android.activium.dialogs.FeedbackDialog;
import com.main.android.activium.ViewModels.MealViewModel;
import com.main.android.activium.dialogs.UpdateDetailsDialog;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MealsRecordActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener,
        FeedbackDialog.InterFeedBack, UpdateDetailsDialog.UpdateDetailsInterface {
    private static final String TAG = "LoggedMeal Record Activity";

    /**
     * declaring global variables
     */
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private DayMealsAdapter mAdapter;


    private Intent intent;
    // shareIntent intent for app sharing
    private Intent shareIntent;

    private RecordsdaysAdapter Daysadapter;
    private Button mPkBtn;
    private TextView mDate;
    private TextView mEmptyStateView;
    private ProgressBar mProgressbar;
    final   Date currDate = Calendar.getInstance().getTime();
    // View Model
    private MealViewModel model;


    // MangoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();

    @Override
    protected void onStart() {
        super.onStart();
        // Verify user is authenticated
        chkAuth();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals_record);

        // Verify user is authenticated
        chkAuth();

        // link views
        drawer = findViewById(R.id.mealRecord_layout); //references main activity
        Toolbar toolBar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view_meals_record);
        /*spinner = findViewById(R.id.spinner);*/
        mPkBtn = findViewById(R.id.pk_day_btn);
        mDate = findViewById(R.id.date);
        // link empty state textView
        mEmptyStateView = findViewById(R.id.empty_state_view);
        // link progressbar view
        mProgressbar = findViewById(R.id.progress_bar);

        // setup toolbar
        toolBar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolBar);
        // menu hamburger button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_black_36);
            getSupportActionBar().setTitle("Meals Record");
        }
        //set up drawer_menu Navigation listener
        setNavigationViewListener();
        // Set drawer state change listener
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

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
            }
        });

        // Initialize layout with present date
        mDate.setText(stringOfDate(currDate));

        // Set ViewModel.
        model = ViewModelProviders.of(this).get(MealViewModel.class);


        // Date picker button listener
        mPkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup and show date picker dialog listener
                Calendar now = Calendar.getInstance();  // instantiate calender to get current time
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MealsRecordActivity.this,
                        now.get(Calendar.YEAR), // Initial year selection
                        now.get(Calendar.MONTH), // Initial month selection
                        now.get(Calendar.DAY_OF_MONTH) // Inital day selection
                );
                // set accent color
                dpd.setAccentColor(getResources().getColor(R.color.colorPrimaryLight));
                // show date picker dialog
                dpd.show(getSupportFragmentManager(), "Datepickerdialog");
            }
        });



        // Adapter setup
        mAdapter = new DayMealsAdapter(this, new ArrayList<LoggedMeal>());
        // link  {@link ListView}
        ListView mListView = findViewById(R.id.list);
        //Set empty state textView
        mListView.setEmptyView(mEmptyStateView);
        // Set list adapter on {@link ListView}
        mListView.setAdapter(mAdapter);

        // Network status check
        ConnectivityManager cm = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //Get the network status info
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        // When net work is available
        if (isConnected) {
            /*Log.d(TAG, "Connection available");*/
            // Create the observer which updates the UI.
            final Observer<ArrayList<LoggedMeal>> mealsObserver = new Observer<ArrayList<LoggedMeal>>() {
                @Override
                public void onChanged(ArrayList<LoggedMeal> meals) {
                    mAdapter.clear();
                    //sets text to empty state textView
                    mEmptyStateView.setText(R.string.no_meals);
                    //Set progressbar view to invisible when when loading is finished
                    mProgressbar.setVisibility(View.GONE);
                    // If there is meals available
                    if (meals != null && !meals.isEmpty()) {
                        // add meals arrayList to adapter
                        mAdapter.addAll(meals);
                    }else{
                        mAdapter.clear();
                        //sets "no meals found"
                        mEmptyStateView.setText(R.string.no_meals);
                        //Set progressbar view to invisible when when loading is finished
                        mProgressbar.setVisibility(View.GONE);
                    }
                }
            };
            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            model.getMeals(currDate).observe(this, mealsObserver);

        } else {
            /*Log.e(TAG, "No connection found");*/
            //sets text to empty state view when there is no data
            mEmptyStateView.setText(R.string.no_internet);
            //Set progressbar view to invisible when when loading is finished
            mProgressbar.setVisibility(View.GONE);
        }
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
            /*case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_meal_plans:
                intent = new Intent(this, MealPlansActivity.class);
                break;
            case R.id.nav_ml_record:
                break;
            case R.id.nav_dt_stats:
                intent = new Intent(this, DietStatsActivity.class);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
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
                break;*/
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("MealsRecord", "Hide soft keyboard error: " + e);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Date date;
        String datePattern = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
        // Clear adapter
        mAdapter.clear();
        //Set progressbar view to invisible when when loading is finished
        mProgressbar.setVisibility(View.VISIBLE);
        // hide emptyText textView
        mEmptyStateView.setVisibility(View.GONE);
        try {
            // Configure date
            date = new SimpleDateFormat("dd/MM/yyyy").parse(datePattern);
            SimpleDateFormat formatter = new SimpleDateFormat("EE");
            String dayName = formatter.format(date);
            String dateName = dayName + " " + dayOfMonth + "." + (monthOfYear + 1) + "." + year;
            // Update UI
            mDate.setText(dateName);
            model.getMeals(date);
        } catch (Exception e) {
            Log.e("MealsRecord", "Error formatting selected dialog date to Date Object type: " + e);
        }
    }

    private void chkAuth(){
        // Check if user is signed in (true). //mongoDB
        if (!stitchClient.getAuth().isLoggedIn()) {
            finish();
            //send user to login layout
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private String stringOfDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        String currDateString = formatter.format(date);
        SimpleDateFormat DayNameFrmt = new SimpleDateFormat("EE");
        String fullDateString = DayNameFrmt.format(date) + " " + currDateString;
        return fullDateString;
    }

    public void showFeedbackDialog() {
        FeedbackDialog feedBackDialog = new FeedbackDialog();
        feedBackDialog.show(getSupportFragmentManager(), "feedBack");
    }

    @Override
    public void triggerSuccessToast() {
        Toast.makeText(MealsRecordActivity.this, "FeedBack Sent. Thank you",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFailureToast() {
        Toast.makeText(MealsRecordActivity.this, "Failed sending feedback!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboard() {
        hideSoftKeyboard(MealsRecordActivity.this);
    }

    @Override
    public void detailsSuccessUpdate() {
        Toast.makeText(MealsRecordActivity.this, "Details updated successfully",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void detailsFailureUpdate() {
        Toast.makeText(MealsRecordActivity.this, "Failed updating details!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboardUpdate() {
        hideSoftKeyboard(MealsRecordActivity.this);
    }
}