package com.main.android.activium;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.main.android.activium.Adapters.TodaysSessionsAdapter;
import com.main.android.activium.Adapters.UpcomingSessionsAdapter;
import com.main.android.activium.DataClasses.Sessions;
import com.main.android.activium.dialogs.FeedbackDialog;
import com.main.android.activium.ViewModels.SessionsViewModel;
import com.main.android.activium.dialogs.UpdateDetailsDialog;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ConsultationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
        UpcomingSessionsAdapter.SechduleSessInterface, UpdateDetailsDialog.UpdateDetailsInterface {

    private Intent intent;
    // shareIntent intent for app sharing
    private Intent shareIntent;
    private TextView emptyTodaysSessionList;
    private ListView todaysSessionList;
    private TextView emptyUpcomingSessionList;
    private ListView upcomingSessionList;
    private Toolbar toolBar;
    private UpcomingSessionsAdapter upcomingSessionsAdapter;
    private TodaysSessionsAdapter todaysSessionsAdapter;

    private SpinKitView upcomingLoadingProgr;
    private SpinKitView todaysLoadingProgr;

    // user info and drawer variables
    private String email;
    private TextView userEmailTextView; // drawer user email txt view
    private ImageView profilePhoto; // drawer user profile photo
    private String profileImgUrl;
    private NavigationView navigationView;
    private View headerView;
    private DrawerLayout drawer;

    // MongoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    final StitchUser user = stitchClient.getAuth().getUser();
    private RemoteMongoCollection sessionCollection;
    private RemoteMongoClient mongoDBClient;


    // View Models
    private SessionsViewModel sessionsViewModel;

    // Upcoming sessions List
    private ArrayList<Sessions> upcomingSessionsList;

    //user id
    private String uId;

    // Id of session to be scheduled
    private String scheduledSessionId;

    // Selected date for scheduling session
    private int selectedYear;
    private int selectedMonth;
    private int selectedday;

    // update_details drawer click check
    private boolean isUpdateDialog;


    /**User authentication ON APP launch*/
    // check if the user is logged in when the app is launched
    @Override
    public void onStart() {
        super.onStart();
        chkAuth();
    }
    // check if the user is logged in when the resumes
    @Override
    protected void onPostResume() {
        super.onPostResume();
        chkAuth();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make sure this is before calling super.onCreate
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultations);

        // Check if user is authenticated
        chkAuth();

        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        sessionCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        // Assign ViewModel.
        sessionsViewModel = ViewModelProviders.of(this).get(SessionsViewModel.class);

        emptyTodaysSessionList = findViewById(R.id.emptyTodaysSess);
        todaysSessionList = findViewById(R.id.todaysSessList);
        emptyUpcomingSessionList = findViewById(R.id.emptyUpcomingSess);
        upcomingSessionList = findViewById(R.id.upcomingSessList);
        toolBar = findViewById(R.id.toolbar_consultations);
        navigationView = findViewById(R.id.nav_view_consultations);
        headerView = navigationView.getHeaderView(0);
        userEmailTextView = headerView.findViewById(R.id.user_email);
        profilePhoto = headerView.findViewById(R.id.profile_pc);
        drawer = findViewById(R.id.Consultation_layout); //references main activity
        upcomingLoadingProgr = findViewById(R.id.upcoming_loading_progr);
        todaysLoadingProgr = findViewById(R.id.todays_loading_progr);


        // setup toolbar and drawer
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
                    "error setting user email to drawer.value: "  + email);
        }
        // menu hamburger button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_black_36);
            getSupportActionBar().setTitle("Consulting Sessions");
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
                    UpdateDetailsDialog updateDetailsDialog = new UpdateDetailsDialog();
                    updateDetailsDialog.show(getSupportFragmentManager(), "Update Details");
                    isUpdateDialog = false;
                }
            }
        });

        // Adapter setup
        upcomingSessionsAdapter = new UpcomingSessionsAdapter(this, new ArrayList<Sessions>());
        todaysSessionsAdapter = new TodaysSessionsAdapter(this, new ArrayList<Sessions>());
        //Set empty state textView
        /*upcomingSessionList.setEmptyView(emptyUpcomingSessionList);*/
        /*todaysSessionList.setEmptyView(emptyTodaysSessionList);*/
        // Set list adapter on {@link ListView}
        upcomingSessionList.setAdapter(upcomingSessionsAdapter);
        todaysSessionList.setAdapter(todaysSessionsAdapter);

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
            final Observer<ArrayList<Sessions>> upcomingSessionsObserver = new Observer<ArrayList<Sessions>>() {
                @Override
                public void onChanged(ArrayList<Sessions> sessions) {
                    //clear list view
                    upcomingSessionsAdapter.clear();
                    // If there is meals available
                    if (sessions != null && !sessions.isEmpty()) {
                        //set current upcoming session list value
                        upcomingSessionsList = sessions;
                        // set  empty state to gone
                        emptyUpcomingSessionList.setVisibility(View.GONE);
                        //Set progressbar view to invisible when when loading is finished
                        upcomingLoadingProgr.setVisibility(View.GONE);
                        // add meals arrayList to adapter
                        upcomingSessionsAdapter.addAll(sessions);
                    }else{
                        upcomingSessionsAdapter.clear();
                        //Set progressbar view to invisible when when loading is finished
                        upcomingLoadingProgr.setVisibility(View.GONE);
                        // set  empty state to visible
                        emptyUpcomingSessionList.setVisibility(View.VISIBLE);
                    }
                }
            };
            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            sessionsViewModel.getUpcomingSessions().observe(this, upcomingSessionsObserver);

            final Observer<ArrayList<Sessions>> todaysSessionsObserver = new Observer<ArrayList<Sessions>>() {
                @Override
                public void onChanged(ArrayList<Sessions> sessions) {
                    todaysSessionsAdapter.clear();
                    // If there is meals available
                    if (sessions != null && !sessions.isEmpty()) {
                        // set  empty state to gone
                        emptyTodaysSessionList.setVisibility(View.GONE);
                        //Set progressbar view to invisible when when loading is finished
                        todaysLoadingProgr.setVisibility(View.GONE);
                        // add meals arrayList to adapter
                        todaysSessionsAdapter.addAll(sessions);
                    }else{
                        todaysSessionsAdapter.clear();
                        //Set progressbar view to invisible when when loading is finished
                        todaysLoadingProgr.setVisibility(View.GONE);
                        // set  empty state to visible
                        emptyTodaysSessionList.setVisibility(View.VISIBLE);
                    }
                }
            };
            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            sessionsViewModel.getTodaysSessions().observe(this, todaysSessionsObserver);
        } else {
            /*Log.e(TAG, "No connection found");*/
            //sets text to empty state view when there is no data
            emptyUpcomingSessionList.setText(R.string.no_upcoming_sess);
            emptyTodaysSessionList.setText(R.string.no_todays_sess);
            //Set progressbar view to invisible when when loading is finished
            upcomingLoadingProgr.setVisibility(View.GONE);
            todaysLoadingProgr.setVisibility(View.GONE);
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
                email = user.getProfile().getEmail();
                profileImgUrl = user.getProfile().getPictureUrl();
            }else{
                Log.e("ConsultationActivity",
                        "User is authenticated but user value is null");
                stitchClient.getAuth().logout(); // log the user out
                //send user to login layout
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return  false;
            }
            return  true;
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
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_meal_plans:
                intent = new Intent(this, MealPlansActivity.class);
                break;
            case R.id.nav_shopping_list:
                intent = new Intent(this, ShoppingListActivity.class);
                break;
            /*case R.id.nav_ml_record:
                intent = new Intent(this, MealsRecordActivity.class);
                break;*/
            /*case R.id.nav_dt_stats:
                intent = new Intent(this, DietStatsActivity.class);
                break;*/
            case R.id.nav_meassages:
                intent = new Intent(this, MessagesActivity.class);
                break;
            case R.id.nav_counselling:
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

    public void showFeedbackDialog() {
        FeedbackDialog feedBackDialog = new FeedbackDialog();
        feedBackDialog.show(getSupportFragmentManager(), "feedBack");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        // Set selected date
        selectedYear = year;
        selectedMonth = monthOfYear;
        selectedday = dayOfMonth;

        // Setup and show date picker dialog listener
        Calendar now = Calendar.getInstance();  // instantiate calender to get current time
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR),
                now.get(Calendar.MINUTE),
                false
        );
        // set accent color
        tpd.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        // show time picker dialog
        tpd.show(getSupportFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        // set scheduled Date
        Calendar scheduledDateCalendar = Calendar.getInstance();
        scheduledDateCalendar.set(selectedYear, selectedMonth, selectedday, hourOfDay, minute, second);
        scheduledDateCalendar.setTimeZone(TimeZone.getDefault());
        Date scheduledDate = scheduledDateCalendar.getTime();
        // Update session
        updateSession(scheduledSessionId, scheduledDate);
        Toast.makeText(this, "Scheduling your session...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void scheduleSession(String sessionId, int position) {
        scheduledSessionId = sessionId;
        // update upcoming session list item view to being scheduled state
        Sessions scheduledSession = upcomingSessionsList.get(position);
        upcomingSessionsList.remove(position);
        scheduledSession.setBeingScheduled(true);
        upcomingSessionsList.add(scheduledSession);
        upcomingSessionsAdapter.clear();
        upcomingSessionsAdapter.addAll(upcomingSessionsList);
        upcomingSessionsAdapter.notifyDataSetChanged();

        // Setup and show date picker dialog listener
        Calendar now = Calendar.getInstance();  // instantiate calender to get current time
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                ConsultationActivity.this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        );
        // set accent color
        dpd.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        // show date picker dialog
        dpd.show(getSupportFragmentManager(), "Datepickerdialog");
    }

    private void updateSession (String sessionId, Date date){

        Document filterDoc = new Document()
                .append("_id", new ObjectId(sessionId))
                .append("consulteeId", uId);
        Document updateDoc = new Document().append("$set", new Document()
                .append("dateTime", date)
                .append("scheduled", true)
                .append("lastModified", Calendar.getInstance().getTime()));

        final Task<RemoteUpdateResult> updateTask =
                sessionCollection.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener<RemoteUpdateResult>() {
            @Override
            public void onComplete(@NonNull Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    /*Log.d("ConsultationActivity", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));*/
                    Toast.makeText(ConsultationActivity.this, "Session scheduled", Toast.LENGTH_LONG).show();
                    // update upcoming sessions list
                    sessionsViewModel.getUpcomingSessions();

                } else {
                    Log.e("main/updateSettings", "failed to update document with: ", task.getException());
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
            Log.e("ConsultationActivity", "Hide soft keyboard error: " + e);
        }
    }

    @Override
    public void detailsSuccessUpdate() {
        Toast.makeText(ConsultationActivity.this, "Details updated successfully",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void detailsFailureUpdate() {
        Toast.makeText(ConsultationActivity.this, "Failed updating details!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboardUpdate() {
        hideSoftKeyboard(ConsultationActivity.this);
    }
}
