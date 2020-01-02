package com.main.android.activium;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.util.DateTime;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.main.android.activium.Adapters.MealPlansAdapter;
import com.main.android.activium.DataClasses.RecomndRecipe;
import com.main.android.activium.ViewModels.MealPlansViewModel;
import com.main.android.activium.dialogs.ConfirmEatenDialog;
import com.main.android.activium.dialogs.FeedbackDialog;
import com.main.android.activium.dialogs.MealRecipeDialog;
import com.main.android.activium.dialogs.UpdateDetailsDialog;
import com.main.android.activium.util.DayBoundaries;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import eu.amirs.JSON;


public class MealPlansActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FeedbackDialog.InterFeedBack,
        UpdateDetailsDialog.UpdateDetailsInterface, MealRecipeDialog.handleRecipe,
        ConfirmEatenDialog.confirmEatenInterface {
    private static final String TAG = "MealPlansActivity";

    /**
     * declaring global variables
     */
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private MealPlansAdapter mAdapter;
    private List<String> listHeaders;
    private HashMap<String, ArrayList<RecomndRecipe>> ListsData;
    private ExpandableListView expandableList;

    private Intent intent;
    // shareIntent intent for app sharing
    private Intent shareIntent;

    private ImageButton dateBackBtn;
    private ImageButton dateForwardBtn;
    private TextView mDateselectedTxtView;
    private TextView mEmptyStateView;
    private ProgressBar mProgressbar;
    // View Model
    private MealPlansViewModel model;

    // MangoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    final StitchUser user = stitchClient.getAuth().getUser();

    // The date currently diplayed
    private Date selectedDate;

    // Drawer variables
    private String uId;
    private String email;
    private TextView userEmailTextView; // drawer user email txt view
    private ImageView profilePhoto; // drawer user profile photo
    private String profileImgUrl;
    private View headerView;

    // nav_menu update_details drawer click check
    private boolean isUpdateDialog;

    // clicked recipeId
    private RecomndRecipe clickedRecipe;

    // back button state
    private boolean backState = true;

    // server api url
    final static String uri = "server_url";

    @Override
    public void onBackPressed() {
        // configure back button
        moveTaskToBack(backState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verify user is authenticated
        chkAuth();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plans);

        // Verify user is authenticated
        chkAuth();

        // link views
        drawer = findViewById(R.id.meal_plans_layout); //references main activity
        Toolbar toolBar = findViewById(R.id.meal_plans_toolbar);
        navigationView = findViewById(R.id.nav_view_meal_plans);
        dateBackBtn = findViewById(R.id.meal_plans_date_back_btn);
        dateForwardBtn = findViewById(R.id.meal_plan_date_forward_btn);
        mDateselectedTxtView = findViewById(R.id.date_selected);
        // link empty state textView
        mEmptyStateView = findViewById(R.id.meal_plans_empty_state_view);
        // link progressbar view
        mProgressbar = findViewById(R.id.meal_plans_progress_bar);
        headerView = navigationView.getHeaderView(0);
        userEmailTextView = headerView.findViewById(R.id.user_email);
        profilePhoto = headerView.findViewById(R.id.profile_pc);
        expandableList = findViewById(R.id.meal_plans_expand_list);

        // setup toolbar
        toolBar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolBar);
        if (profileImgUrl != null){
            // set drawer profile image
            Glide.with(this).load(profileImgUrl).apply(new RequestOptions()
                    .override(150, 150)).apply(RequestOptions.circleCropTransform())
                    .into(profilePhoto);
        }else {
            Log.e(TAG,
                    "error setting use profile photo to profileImgUrl.value: "  + profileImgUrl);
        }
        if(email != null){
            // set drawer user email
            userEmailTextView.setText(email);
        }else {
            Log.e(TAG,
                    "error setting use email to drawer.value: "  + userEmailTextView);
        }
        // menu hamburger button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_black_36);
            getSupportActionBar().setTitle("Meal Plans");
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

        selectedDate = Calendar.getInstance().getTime();

        // Set initial date selected string
        setSelectedDateText(selectedDate);

        // Set ViewModel.
        model = ViewModelProviders.of(this).get(MealPlansViewModel.class);

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
            final Observer<ArrayList<RecomndRecipe>> mealPlansObserver = new Observer<ArrayList<RecomndRecipe>>() {
                @Override
                public void onChanged(ArrayList<RecomndRecipe> meals) {
                    mProgressbar.setVisibility(View.GONE);
                    mEmptyStateView.setVisibility(View.GONE);
                    if (meals != null && !meals.isEmpty()) {
                        // Hide list
                        expandableList.setVisibility(View.GONE);
                        // Set Lists
                        listHeaders = new ArrayList<String>();
                        ListsData = new HashMap<String, ArrayList<RecomndRecipe>>();
                        ArrayList<RecomndRecipe> breakfastRecmnds = new ArrayList<RecomndRecipe>();
                        ArrayList<RecomndRecipe> lunchRecmnds = new ArrayList<RecomndRecipe>();
                        ArrayList<RecomndRecipe> dinnerRecmnds = new ArrayList<RecomndRecipe>();
                        ArrayList<RecomndRecipe> snacksRecmnds = new ArrayList<RecomndRecipe>();

                        // Set headers
                        listHeaders.add("BREAKFAST");
                        listHeaders.add("LUNCH");
                        listHeaders.add("DINNER");
                        listHeaders.add("SNACKS");

                        for(RecomndRecipe recomndRecipe: meals){
                            if(recomndRecipe.getIsBreakfast()){
                                breakfastRecmnds.add(recomndRecipe);
                            }
                            else if(recomndRecipe.getIsLunch()){
                                lunchRecmnds.add(recomndRecipe);
                            }
                            else if(recomndRecipe.getIsDinner()){
                                dinnerRecmnds.add(recomndRecipe);
                            }
                            else if(recomndRecipe.getIsSnack()){
                                snacksRecmnds.add(recomndRecipe);
                            }
                        }

                        // add all lists to data hash map
                        ListsData.put(listHeaders.get(0), breakfastRecmnds);
                        ListsData.put(listHeaders.get(1), lunchRecmnds);
                        ListsData.put(listHeaders.get(2), dinnerRecmnds);
                        ListsData.put(listHeaders.get(3), snacksRecmnds);

                        // Adapter setup
                        mAdapter = new MealPlansAdapter(MealPlansActivity.this, listHeaders, ListsData);

                        // set adapter to list
                        expandableList.setAdapter(mAdapter);

                        // Set on list item clicked listener
                        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                            @Override
                            public boolean onChildClick(ExpandableListView expandableListView, View view,
                                                        int groupPosition, int childPosition, long l) {
                                // set recipe dialog and listener
                                MealRecipeDialog mealRecipeDialog = new MealRecipeDialog(
                                        MealPlansActivity.this, R.style.AppTheme);

                                // determine recipe clicked based on position and group index
                                if (groupPosition == 0) {
                                    clickedRecipe = breakfastRecmnds.get(childPosition);
                                }
                                if (groupPosition == 1) {
                                    clickedRecipe = lunchRecmnds.get(childPosition);
                                }
                                if (groupPosition == 2) {
                                    clickedRecipe = dinnerRecmnds.get(childPosition);
                                }
                                if (groupPosition == 3) {
                                    clickedRecipe = snacksRecmnds.get(childPosition);
                                }

                                if (!clickedRecipe.getIsPreped()) {
                                    mealRecipeDialog.setRecipe(clickedRecipe);
                                    mealRecipeDialog.setSelctedDate(selectedDate);
                                    mealRecipeDialog.setOwnerActivity(MealPlansActivity.this);
                                    backState = false;
                                    showMealRecipeDialog(mealRecipeDialog);
                                }else{
                                    if(!clickedRecipe.getEaten()) {
                                        // do nothing if the meal is not prepped for today
                                        Date currDate = Calendar.getInstance().getTime();
                                        DayBoundaries today = new DayBoundaries(currDate);
                                        if (selectedDate.compareTo(today.getDayEnd()) <= 0) {
                                            ConfirmEatenDialog confirmEatenDialog = new ConfirmEatenDialog();
                                            confirmEatenDialog.setmRecomndRecipe(clickedRecipe); //set recomndId of item clicked
                                            confirmEatenDialog.show(getSupportFragmentManager(), "Confirm eaten");
                                        } else {
                                            Toast.makeText(MealPlansActivity.this, "Can't mark future meals as eaten", Toast.LENGTH_LONG);
                                        }
                                    }
                                }
                                return false;
                            }
                        });
                        // Show list
                        expandableList.setVisibility(View.VISIBLE);
                    }else{
                        // Hide list
                        expandableList.setVisibility(View.GONE);
                        mProgressbar.setVisibility(View.GONE);
                        mEmptyStateView.setText(R.string.empty_meal_plans);
                        mEmptyStateView.setVisibility(View.VISIBLE);
                    }
                }
            };
            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            model.getUiData(Calendar.getInstance().getTime(), uId).observe(this, mealPlansObserver);
        } else {
            /*Log.e(TAG, "No connection found");*/
            // Hide list
            expandableList.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.GONE);
            mEmptyStateView.setText(R.string.no_internet);
            mEmptyStateView.setVisibility(View.VISIBLE);
        }

        // Backward date button listener
        dateBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide list
                expandableList.setVisibility(View.GONE);
                // Set UI
                mEmptyStateView.setVisibility(View.GONE);
                mProgressbar.setVisibility(View.VISIBLE);
                Calendar c = Calendar.getInstance();
                c.setTime(selectedDate);
                c.add(Calendar.DAY_OF_MONTH, -1);
                selectedDate = c.getTime();
                model.getUiData(selectedDate, uId);
                setSelectedDateText(selectedDate);
            }
        });

        // Forward date button listener
        dateForwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide list
                expandableList.setVisibility(View.GONE);
                // Set UI
                mEmptyStateView.setVisibility(View.GONE);
                mProgressbar.setVisibility(View.VISIBLE);
                Calendar c = Calendar.getInstance();
                c.setTime(selectedDate);
                c.add(Calendar.DAY_OF_MONTH, 1);
                selectedDate = c.getTime();
                model.getUiData(selectedDate, uId);
                setSelectedDateText(selectedDate);
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
            case R.id.nav_shopping_list:
                intent = new Intent(this, ShoppingListActivity.class);
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
        Toast.makeText(MealPlansActivity.this, "FeedBack Sent. Thank you",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFailureToast() {
        Toast.makeText(MealPlansActivity.this, "Failed sending feedback!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboard() {
        hideSoftKeyboard(MealPlansActivity.this);
    }

    @Override
    public void detailsSuccessUpdate() {
        Toast.makeText(MealPlansActivity.this, "Details updated successfully",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void detailsFailureUpdate() {
        Toast.makeText(MealPlansActivity.this, "Failed updating details!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboardUpdate() {
        hideSoftKeyboard(MealPlansActivity.this);
    }

    private void setSelectedDateText(Date dateSelected){
        // string date formatter
        SimpleDateFormat Dateformatter = new SimpleDateFormat("dd.MM", Locale.getDefault());
        SimpleDateFormat Dayformatter = new SimpleDateFormat("EE", Locale.getDefault());
        // Date string
        String dateSelectedString = Dayformatter.format(dateSelected) + " " + Dateformatter.format(dateSelected);
        // Set text
        mDateselectedTxtView.setText(dateSelectedString);
    }

    public void showMealRecipeDialog(MealRecipeDialog mealRecipeDialog) {
        mealRecipeDialog.show();
    }



    @Override
    public void setEaten(RecomndRecipe recipe) {
        /*// do nothing if the meal is not prepped for today
        Date currDate = Calendar.getInstance().getTime();
        DayBoundaries today = new DayBoundaries(currDate);
        if (selectedDate.compareTo(today.getDayEnd()) <= 0) {
            // show loading and hide list
            expandableList.setVisibility(View.GONE);
            //remove recipe from list
            *//*if(recomndRecipe.getIsBreakfast()){
                recomndListAdapter.remove(recomndRecipe);
            }*//*
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            *//*Log.d(TAG, "dateof meal: " + dateFormat.format(date));*//*
            //enable back button
            backState = true;
            // log eaten meal with backend call
            makeServerCall(user.getId(), "log_old_meal", null, null, null,
                    recipe.getRecipeId(), recipe.getRecomndId(), dateFormat.format(date));
        }else{
            Toast.makeText(this, "Can't mark future meals as eaten", Toast.LENGTH_LONG);
        }*/
    }

    @Override
    public void setPrepForLater(RecomndRecipe recipe) {
        // show loading and hide list
        expandableList.setVisibility(View.GONE);
        mProgressbar.setVisibility(View.VISIBLE);
        // mark recommended recipe as preped for later
        setAsPrepedForLater(recipe.getRecomndId());
    }

    private void setAsPrepedForLater(String recomndID){
        //initialize mongoDB remote stitchClient
        RemoteMongoClient mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        RemoteMongoCollection recomndsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        Document filterDoc = new Document().append("_id", new ObjectId(recomndID));
        Document updateDoc = new Document().append("$set",
                new Document()
                        .append("isPreped", true)
        );


        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);
        final Task<RemoteUpdateResult> updateTask = recomndsCollection.updateOne(filterDoc, updateDoc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener<RemoteUpdateResult>() {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    /*Log.d(TAG, String.format("successfully matched %d and modified" +
                            " %d documents @setAsPrepedForLater", numMatched, numModified));*/
                    // Update ui
                    model.getUiData(selectedDate, uId);
                } else {
                    Log.e(TAG, "failed to update document @setAsPrepedForLater" +
                            " error: ", task.getException());
                }
            }
        });
    }

    // Query backend server
    public void makeServerCall(String uId, String intent, String food, String measure,
                               Double quantity, String recipeId, String recomndId, String dateString) {
        /*Log.d("MainActiviy", "fetchEarthquakeData initialized");*/

        // Create URL object
        URL url = makeUrl(uri);

        // Perform HTTP request to the URL and receive a JSON response back
        try {
            makeHTTPRequest(url, uId, intent, food, measure, quantity, recipeId, recomndId, dateString);
        } catch (IOException e) {
            Log.e("MainActiviy", "Error making Http request to server", e);
        }

    }

    private static URL makeUrl(String urlString){
        URL url = null;
        try {
            url = new URL(urlString);
        }catch(MalformedURLException e){
            Log.e(TAG, "Problem making URL object from string", e);
        }
        return url;
    }

    private void makeHTTPRequest(URL url,String uId, String intent, String food, String measure, Double quantity, String recipeId, String recomndId, String dateString) throws IOException {
        // handle asynchronously
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids) {
                String jsonResponse = "";
                HttpURLConnection urlConnection= null;
                InputStream inputStream = null;

                // If the URL is null, then return early.
                if (url == null) {
                    return jsonResponse;
                }

                // build json object with  query parameters
                String jsonString = "{\"responseId\": \"\"}";

                /*Log.d("MainActiviy", "uriString: " + jsonString);*/
                JSON jsonParam = new JSON(jsonString);

                try{
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(10000 /* milliseconds */);
                    urlConnection.setConnectTimeout(15000 /* milliseconds */);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(jsonParam.toString());
                    Log.i(MainActivity.class.toString(), jsonParam.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    urlConnection.connect();
                    // If the request was successful (response code 200),
                    // then read the input stream and parse the response.
                    if (urlConnection.getResponseCode() == 200) {
                        inputStream = urlConnection.getInputStream();
                        jsonResponse = readFromStream(inputStream);
                        /*Log.d("MainActiviy", "jsonResponse: " + jsonResponse);*/
                    } else {
                        Log.e("MainActiviy", "Error response code: " + urlConnection.getResponseCode());
                    }
                } catch (IOException e) {
                    Log.e("MainActiviy", "Problem retrieving the earthquake JSON results.", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }catch(Exception e){
                            Log.e("MainActiviy", "inputStream error: " + e);
                        }
                    }
                }
                return jsonResponse;
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                JSON jsonResponse = new JSON(response);
                String responseString;
                try {
                    responseString = jsonResponse.getJsonObject().getString("fulfillmentText");
                }catch (Exception e){
                    responseString = null;
                    Log.e(TAG, "Error processing server json response");
                }
                // Update ui
                model.getUiData(selectedDate, uId);
                if (responseString != null){
                    Toast.makeText(MealPlansActivity.this, responseString, Toast.LENGTH_LONG).show();
                }
                /*Log.d(TAG, "server response: " + responseString);*/
            }
        }.execute();
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    @Override
    public void confirmEaten(RecomndRecipe recomndRecipe) {
        /*// do nothing if the meal is not prepped for today
        Date currDate = Calendar.getInstance().getTime();
        DayBoundaries today = new DayBoundaries(currDate);
        if (selectedDate.compareTo(today.getDayEnd()) <= 0) {
            // show loading and hide list
            expandableList.setVisibility(View.GONE);
            //remove recipe from list
            *//*if(recomndRecipe.getIsBreakfast()){
                recomndListAdapter.remove(recomndRecipe);
            }*//*
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            *//*Log.d(TAG, "dateof meal: " + dateFormat.format(date));*//*
            //enable back button
            backState = true;
            // log eaten meal with backend call
            makeServerCall(user.getId(), "log_old_meal", null, null, null,
                    recomndRecipe.getRecipeId(), recomndRecipe.getRecomndId(), dateFormat.format(date));
        }else{
            Toast.makeText(this, "Can't mark future meals as eaten", Toast.LENGTH_LONG);
        }*/
    }
}