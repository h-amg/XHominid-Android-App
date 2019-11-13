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
import android.widget.Button;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.main.android.activium.Adapters.ShoppingListAdapter;
import com.main.android.activium.DataClasses.ShoppingItem;
import com.main.android.activium.ViewModels.ShoppingListViewModel;
import com.main.android.activium.dialogs.FeedbackDialog;
import com.main.android.activium.dialogs.UpdateDetailsDialog;
import com.main.android.activium.util.WeekBoundaries;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ShoppingListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FeedbackDialog.InterFeedBack,
        UpdateDetailsDialog.UpdateDetailsInterface {
    private static final String TAG = "LoggedMeal Record Activity";

    /**
     * declaring global variables
     */
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ShoppingListAdapter mAdapter;

    private Intent intent;
    // shareIntent intent for app sharing
    private Intent shareIntent;

    private Button pickPeriodBtn;
    private TextView mPeriodSelectedView;
    private TextView mEmptyStateView;
    private ProgressBar mProgressbar;
    // View Model
    private ShoppingListViewModel model;

    // MangoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    final StitchUser user = stitchClient.getAuth().getUser();

    // State check point for next shopping list period yo be viewed
    private boolean showNextWeek = true;

    // Drawer variables
    private String uId;
    private String email;
    private TextView userEmailTextView; // drawer user email txt view
    private ImageView profilePhoto; // drawer user profile photo
    private String profileImgUrl;
    private View headerView;

    // update_details drawer click check
    private boolean isUpdateDialog;

    @Override
    protected void onStart() {
        super.onStart();
        // Verify user is authenticated
        chkAuth();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Verify user is authenticated
        chkAuth();

        // link views
        drawer = findViewById(R.id.shopping_layout); //references main activity
        Toolbar toolBar = findViewById(R.id.shopping_list_toolbar);
        navigationView = findViewById(R.id.nav_view_shopping_list);
        pickPeriodBtn = findViewById(R.id.shopping_llist_pk_period_btn);
        mPeriodSelectedView = findViewById(R.id.period_selected);
        // link empty state textView
        mEmptyStateView = findViewById(R.id.shopping_list_empty_state_view);
        // link progressbar view
        mProgressbar = findViewById(R.id.shopping_list_progress_bar);
        headerView = navigationView.getHeaderView(0);
        userEmailTextView = headerView.findViewById(R.id.user_email);
        profilePhoto = headerView.findViewById(R.id.profile_pc);

        // setup toolbar
        toolBar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolBar);
        if (profileImgUrl != null){
            // set drawer profile image
            Glide.with(this).load(profileImgUrl).apply(new RequestOptions()
                    .override(150, 150)).apply(RequestOptions.circleCropTransform())
                    .into(profilePhoto);
        }else {
            Log.e("MainActivity",
                    "error setting use profile photo to profileImgUrl.value: "  + profileImgUrl);
        }
        if(email != null){
            // set drawer user email
            userEmailTextView.setText(email);
        }else {
            Log.e("MainActivity",
                    "error setting use email to drawer.value: "  + userEmailTextView);
        }
        // menu hamburger button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_black_36);
            getSupportActionBar().setTitle("Shopping list");
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
                if(isUpdateDialog){
                    UpdateDetailsDialog updateDetailsDialog = new UpdateDetailsDialog();
                    updateDetailsDialog.show(getSupportFragmentManager(), "Update Details");
                    isUpdateDialog = false;
                }
            }
        });
        // Starr/end dates for current week and the week after
        WeekBoundaries thisWeek = new WeekBoundaries();
        Date weekStart = thisWeek.getmWeekStart();
        Date weekEnd = thisWeek.getmWeekEnd();
        Date nextWeekStart = thisWeek.getmNextWeekStart();
        Date nextWeekEnd = thisWeek.getmNextWeekEnd();
        // string date formatter
        SimpleDateFormat Dateformatter = new SimpleDateFormat("dd.MM", Locale.getDefault());
        SimpleDateFormat Dayformatter = new SimpleDateFormat("EE", Locale.getDefault());
        // formatted strung dates (This week)
        String weekStartString = Dayformatter.format(weekStart) + " " + Dateformatter.format(weekStart);
        String weekEndString = Dayformatter.format(weekEnd) + " " + Dateformatter.format(weekEnd);
        String currWeekStartEnd = weekStartString + " - " + weekEndString;
        // formatted strung dates (Next week)
        String nextWeekStartString = Dayformatter.format(nextWeekStart) + " " + Dateformatter.format(nextWeekStart);
        String nextWeekEndString = Dayformatter.format(nextWeekEnd) + " " + Dateformatter.format(nextWeekEnd);
        String nextWeekStartEnd = nextWeekStartString + " - " + nextWeekEndString;

        // Initialize layout with present week
        mPeriodSelectedView.setText(currWeekStartEnd);

        // Set ViewModel.
        model = ViewModelProviders.of(this).get(ShoppingListViewModel.class);

        // Adapter setup
        mAdapter = new ShoppingListAdapter(this, new ArrayList<ShoppingItem>());
        // link  {@link ListView}
        ListView mListView = findViewById(R.id.shopping_list);
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
            final Observer<ArrayList<ShoppingItem>> shoppingListObserver = new Observer<ArrayList<ShoppingItem>>() {
                @Override
                public void onChanged(ArrayList<ShoppingItem> ShoppingList) {
                    mAdapter.clear();
                    mProgressbar.setVisibility(View.GONE);
                    mEmptyStateView.setVisibility(View.GONE);
                    if (ShoppingList != null && !ShoppingList.isEmpty()) {
                        // add meals arrayList to adapter
                        mAdapter.addAll(ShoppingList);
                    }else{
                        mAdapter.clear();
                        mProgressbar.setVisibility(View.GONE);
                        mEmptyStateView.setText(R.string.empty_shopping_list);
                        mEmptyStateView.setVisibility(View.VISIBLE);
                    }
                }
            };
            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            model.getShoppingItems(false).observe(this, shoppingListObserver);
        } else {
            /*Log.e(TAG, "No connection found");*/
            mProgressbar.setVisibility(View.GONE);
            mEmptyStateView.setText(R.string.no_internet);
            mEmptyStateView.setVisibility(View.VISIBLE);
        }

        // Date picker button listener
        pickPeriodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showNextWeek){
                    showNextWeek = false;
                    mAdapter.clear();
                    pickPeriodBtn.setText(R.string.pick_period_this_wk);
                    mEmptyStateView.setVisibility(View.GONE);
                    mProgressbar.setVisibility(View.VISIBLE);
                    model.getShoppingItems(true);
                    mPeriodSelectedView.setText(nextWeekStartEnd);
                    Toast.makeText(ShoppingListActivity.this, "Next week shopping list"
                            , Toast.LENGTH_LONG).show();
                }else{
                    showNextWeek = true;
                    mAdapter.clear();
                    pickPeriodBtn.setText(R.string.pick_period_Next_wk);
                    mEmptyStateView.setVisibility(View.GONE);
                    mProgressbar.setVisibility(View.VISIBLE);
                    model.getShoppingItems(false);
                    mPeriodSelectedView.setText(currWeekStartEnd);
                    Toast.makeText(ShoppingListActivity.this, "This week shopping list"
                            , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setNavigationViewListener() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_meal_plans:
                intent = new Intent(this, MealPlansActivity.class);
                break;
            case R.id.nav_shopping_list:
                break;
            /*case R.id.nav_dt_stats:
                intent = new Intent(this, DietStatsActivity.class);
                break;*/
            case R.id.nav_meassages:
                intent = new Intent(this, MessagesActivity.class);
                break;
            case R.id.nav_counselling:
                intent = new Intent(this, ConsultationActivity.class);
                break;
            case R.id.nav_update_details:
                isUpdateDialog = true;
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
                break;
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

    private boolean chkAuth(){
        // Check if user is signed in (true). //mongoDB
        if (!stitchClient.getAuth().isLoggedIn()) {
            finish();
            //send user to login layout
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return  false;
        }else{
            if(user != null){
                uId = user.getId();
                /*Log.d("MainActivity", "user Id: " + uId);*/
                email = user.getProfile().getEmail();
                profileImgUrl = user.getProfile().getPictureUrl();
            }else{
                Log.e("ShoppingListActivity", "Iser is authenticated but user value is null");
                stitchClient.getAuth().logout(); // log the user out
                //send user to login layout
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return  false;
            }
            return  true;
        }
    }

    public void showFeedbackDialog() {
        FeedbackDialog feedBackDialog = new FeedbackDialog();
        feedBackDialog.show(getSupportFragmentManager(), "feedBack");
    }

    @Override
    public void triggerSuccessToast() {
        Toast.makeText(ShoppingListActivity.this, "FeedBack Sent. Thank you",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFailureToast() {
        Toast.makeText(ShoppingListActivity.this, "Failed sending feedback!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboard() {
        hideSoftKeyboard(ShoppingListActivity.this);
    }

    @Override
    public void detailsSuccessUpdate() {
        Toast.makeText(ShoppingListActivity.this, "Details updated successfully",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void detailsFailureUpdate() {
        Toast.makeText(ShoppingListActivity.this, "Failed updating details!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboardUpdate() {
        hideSoftKeyboard(ShoppingListActivity.this);
    }
}