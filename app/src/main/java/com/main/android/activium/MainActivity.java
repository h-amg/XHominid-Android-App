package com.main.android.activium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.main.android.activium.Adapters.RecomndsAdapter;
import com.main.android.activium.DataClasses.DailyMac;
import com.main.android.activium.DataClasses.Diet;
import com.main.android.activium.DataClasses.MainUiData;
import com.main.android.activium.DataClasses.RecomndRecipe;
import com.main.android.activium.dialogs.ConfirmEatenDialog;
import com.main.android.activium.dialogs.FeedbackDialog;
import com.main.android.activium.dialogs.MealRecipeDialog;
import com.main.android.activium.dialogs.RateEnergyDialog;
import com.main.android.activium.dialogs.RateHungerDialog;
import com.main.android.activium.dialogs.UpdateDetailsDialog;
import com.main.android.activium.dialogs.UserInfoDialog;
import com.main.android.activium.ViewModels.MainUiViewModel;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;
import com.vaibhavlakhera.circularprogressview.CircularProgressView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;

import eu.amirs.JSON;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        UserInfoDialog.UserInfoListener, MealRecipeDialog.handleRecipe,
        FeedbackDialog.InterFeedBack, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, ConfirmEatenDialog.confirmEatenInterface,
        RateEnergyDialog.rateEnergyInterface, RateHungerDialog.rateHungerInterface,
        UpdateDetailsDialog.UpdateDetailsInterface {

    private static final String TAG = "MainActivity";

    // clicked recipeId
    private RecomndRecipe clickedRecipe;

    // audio recorder variables
    private static final int REQUEST_RECORD_AUDIO = 400;
    private static final String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "/recorded_audio.wav";

    // permission request  code
    private final int MY_PERMISSIONS_RECORD_AUDIO = 500;
    private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 600;

    //// Variables and Objects ////
    // user vs amani identifiers
    /*private static final int AMANI = 10002;*/
    // Current Date
    final Date currDay = Calendar.getInstance().getTime();

    // animation
    /*private Animation IndicatorAnim;*/
    // activity intent
    private Intent intent;
    // shareIntent intent for app sharing
    private Intent shareIntent;

    //// Views ////
/*    private TextView amani;
    private ImageButton scroll_indic;
    private SpinKitView spin_kit;
    *//*private CircularProgressView mprog_cal; //calories*/
    private CircularProgressView mprog_prot; //protein
    private CircularProgressView mprog_carbs; //carbs
    private CircularProgressView mprog_fat; //fat
    private Toolbar toolBar;
    private DrawerLayout drawer;
    private LinearLayout mMacros_sec;
 /*   private Button sendBtn;
    private ImageButton recordBtn;
    private EditText foodInput;
    private EditText quantityInput;
    private Spinner measureSpinner;
    private Spinner actionSpinner;*/
    private RecomndsAdapter recomndListAdapter;
    private List<String> listHeaders;
    private HashMap<String, ArrayList<RecomndRecipe>> ListsData;
    private ExpandableListView expandableList;
    private ProgressBar mTotalProg;
    private TextView mEmptyMealsNoSessSched;
    private TextView mPreparingStateMealsView;
    private SpinKitView mPreparingProgress;
    private SpinKitView mMealLoadingProgr;
    private Button scheduleSessionBtn;

    // shared preference persistent memory
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;

    // Input  variables
 /*   private String queryIntent = "log_macro";
    private String food;
    private Double quantity;
    private String measure = "gram";*/


    // MongoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoCollection usersCollection;
    private RemoteMongoCollection dietsCollection;
    private RemoteMongoCollection sessionCollection;
    private RemoteMongoCollection recomndsCollection;
    private RemoteMongoCollection subscriptionsCollection;
    private RemoteMongoCollection energyHungerCollection;
    private RemoteMongoCollection recordingsCollection;
    private RemoteMongoCollection settingsCollection;
    private RemoteMongoClient mongoDBClient;
    private SharedPreferences pseudoCache;
    private SharedPreferences chkPoint;
    final   StitchUser user = stitchClient.getAuth().getUser();

    // Drawer variables
    private String uId;
    private String email;
    private TextView userEmailTextView; // drawer user email txt view
    private ImageView profilePhoto; // drawer user profile photo
    private String profileImgUrl;
    private NavigationView navigationView;
    private View headerView;

    // View Model
    private MainUiViewModel UImodel;

    // firebase storage
    FirebaseStorage storage;

    // server api url
    final static String uri = "server_url";
  

    // weekly craving progress bar
    ProgressDialog cravingRecordProgr;

    // back button state
    private boolean backState = true;

    // Selected date for scheduling session
    private int selectedYear;
    private int selectedMonth;
    private int selectedday;

    // upcoming text TextView
    private TextView upcomingSessNoMeal;
    // upcoming button
    private Button gotToSessBtn;

    // firebase messaging token
    public String mToken;

    // Energy rating
    private int mEnergyRating;

    // Connectivity status
    private ConnectivityManager cm;
    private NetworkInfo activeNetwork;
    private boolean isConnected;

    // update_details drawer click check
    private boolean isUpdateDialog;

    // Billing
    private BillingClient billingClient;

    // Subscription verification attempts
    private int subVeriftAtmp = 0;

    private boolean authenticated = false;

    /**User authentication ON APP launch*/
    // check if the user is logged in when the app is launched
    @Override
    public void onStart() {
        super.onStart();
        if(!authenticated) {
            authenticated = chkAuth();
        }
    }
    // check if the user is logged in when the resumes
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(!authenticated) {
            authenticated = chkAuth();
        }
    }

    @Override
    public void onBackPressed() {
        // configure back button
        moveTaskToBack(backState);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initiate activity
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check google play services sdk availability and allow user to download it if not available
        /*GoogleApiAvailability.makeGooglePlayServicesAvailable()*/

        // check network status and set UI data observer
        cm = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //Get the network status info
        activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // instantiate firebase storage for audio recordings
        storage = FirebaseStorage.getInstance();

        // Set ViewModelS.
        UImodel = ViewModelProviders.of(this).get(MainUiViewModel.class);

        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        usersCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");
        dietsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");
        recordingsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");
        settingsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");
        sessionCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        // initialized shared preferences and retrieved required data
        pseudoCache = getSharedPreferences("pseudoCache", MODE_PRIVATE);


        // initialized shared preferences and retrieved required data
        settings = getSharedPreferences("settings", MODE_PRIVATE);
        settingsEditor = settings.edit();

        // check user settings is synced
        boolean syncStatus = settings.getBoolean("syncState", false);
        if(!syncStatus && user != null){
            updateSettings();
        }

        // initiate weekly cravings progress dialog
        cravingRecordProgr = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);

        // link views
        final View activityRootView = findViewById(R.id.content_main);
        /*spin_kit = findViewById(R.id.spin_kit);*/
        /*mprog_cal = findViewById(R.id.progView_cal);*/
        mprog_prot = findViewById(R.id.progView_prot);
        mprog_carbs = findViewById(R.id.progView_carbs);
        mprog_fat = findViewById(R.id.progView_fat);
        toolBar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.main_layout); //references main activity
        mMacros_sec = findViewById(R.id.macros_sec);
/*        scroll_indic = findViewById(R.id.scroll_indic);
        amani = findViewById(R.id.amani_text);
        btnSpeak = findViewById(R.id.talk);
        sendBtn = findViewById(R.id.send);
        foodInput = findViewById(R.id.text_input);
        quantityInput = findViewById(R.id.qty_input);*/
        mTotalProg = findViewById(R.id.total_prog);
        navigationView = findViewById(R.id.nav_view_main_activity);
        headerView = navigationView.getHeaderView(0);
        userEmailTextView = headerView.findViewById(R.id.user_email);
        profilePhoto = headerView.findViewById(R.id.profile_pc);
        /*recordBtn = findViewById(R.id.record);*/
        scheduleSessionBtn = findViewById(R.id.scheduleSess_btn);
        mEmptyMealsNoSessSched = findViewById(R.id.emptyMeals); // no meal planned, no session scheduled
        mPreparingStateMealsView = findViewById(R.id.preparingText);
        mPreparingProgress = findViewById(R.id.analyzing_progress);
        mMealLoadingProgr = findViewById(R.id.meal_loading_progr);
        upcomingSessNoMeal = findViewById(R.id.upcoming_sess_no_meals);
        gotToSessBtn = findViewById(R.id.go_to_sess_btn);
        expandableList = findViewById(R.id.expandable_recomnds_list);

        // setup toolbar and drawer
        toolBar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolBar);
        if (profileImgUrl != null){
            // set drawer profile image
            Glide.with(this).load(profileImgUrl).apply(new RequestOptions()
                    .override(150, 150)).apply(RequestOptions.circleCropTransform())
                    .into(profilePhoto);
        }else {
           // load default image or ask user for profile image
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
            getSupportActionBar().setTitle("Home");
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


        // hide dash board container when keyboard is visible
        activityRootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int heightDiff = activityRootView.getRootView()
                                .getHeight() - activityRootView.getHeight();
                        if (heightDiff > dpToPx(getApplicationContext(), 200)) { // if more than 200 dp, it's probably a keyboard...
                            mMacros_sec.setVisibility(View.GONE);
                        }else{
                            new Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            mMacros_sec.setVisibility(View.VISIBLE);
                                        }
                                    }, 100);
                        }
                    }
                });

/*
        // set scroll indicator animation
        IndicatorAnim = new AlphaAnimation(1, 0); //to change visibility from visible to invisible

        // set vertical scrolling touch listener for amani textView
        amani.setMovementMethod(new ScrollingMovementMethod());
        amani.setText(R.string.amani_init);
        scrollIndicBlinkEffect(IndicatorAnim);

        // stop animation scroll to bottom when indicator or text touched touched
        scroll_indic.setOnTouchListener(new CustomTouchListener());

        Scroller scroller = new Scroller(this);
        scroller.getFinalY();
        amani.setScroller(scroller);

        // Measure
        List<String> measureList = new ArrayList<String>();
        measureList.add("Gram");
        measureList.add("Ounce");
        measureList.add("Tbsp");
        measureList.add("Tsp");
        measureList.add("Cup");
        ArrayAdapter<String> measureArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.support_simple_spinner_dropdown_item, measureList);
        measureSpinner = findViewById(R.id.measure);
        measureSpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        measureSpinner.setDropDownVerticalOffset(-measureSpinner.getHeight());
        measureSpinner.setAdapter(measureArrayAdapter);
        measureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        measure = "gram";
                        break;
                    case 1:
                        measure = "ounce";
                        break;
                    case 2:
                        measure = "tbsp";
                        break;
                    case 3:
                        measure = "tsp";
                        break;
                    case 4 :
                        measure = "cup";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Action selector
        List<String> actionList = new ArrayList<String>();
        actionList.add("LOG FOOD ITEM");
        actionList.add("CHECK DIET COMPATIBILITY");
        actionList.add("CHECK NUTRITIONAL CONTENT");
        ArrayAdapter<String> actionArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.support_simple_spinner_dropdown_item, actionList);
        actionSpinner = findViewById(R.id.action_selector);
        actionSpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        actionSpinner.setDropDownVerticalOffset(-actionSpinner.getHeight());
        actionSpinner.setAdapter(actionArrayAdapter);
        actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        queryIntent = "log_macro";
                        break;
                    case 1:
                        queryIntent = "check_diet";
                        break;
                    case 2:
                        queryIntent = "macro_query";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

*/

        //retrieve text from editText view
        /*foodInput.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        *//*sendMessage(sendBtn);*//*
                        scroll_indic.setVisibility(View.GONE);
                        IndicatorAnim.cancel();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });*/

        /*// initiate DialogFlow
        initDialogFlow();*/


        // When net work is available
        if (isConnected) {
            final Observer<MainUiData> UiDataObserver = new Observer<MainUiData>() {
                @Override
                public void onChanged(MainUiData mainUiData) {
                    // update UI data
                    setUiData(mainUiData.getdietDoc(), mainUiData.getDailyMacDoc(),
                            mainUiData.getRecomndsList());
                }
            };
            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            UImodel.getUiData(currDay, uId).observe(this, UiDataObserver);
        }

        /*// set send button onclick listener
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    hideSoftKeyboard(MainActivity.this);
                    food = foodInput.getText().toString();
                    quantity = Double.valueOf(quantityInput.getText().toString());
                    makeServerCall(user.getId(), queryIntent, food, measure, quantity,
                            null, 0);
                    // clear and setup ui
                    scroll_indic.setVisibility(View.GONE);
                    IndicatorAnim.cancel();
                    amani.setVisibility(View.GONE);
                    amani.scrollTo(0,0);
                    spin_kit.setVisibility(View.VISIBLE);
                    foodInput.setText(""); // empty edit text
                    quantityInput.setText("");

                }else{
                    Toast.makeText(MainActivity.this, "No internet connection!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });*/

        // record button click listener
        /*recordBtn.setOnClickListener(this::record);*/

        // schedule session  button click listener
        scheduleSessionBtn.setOnClickListener(this::scheduleSession);

        // go to sessions button listener
        gotToSessBtn.setOnClickListener(this::goToSess);

        Bundle intentData = getIntent().getExtras();
        /*Log.d(TAG, "rate energy, intentData: " + intentData);*/
        boolean isRateEnergy = false;
        if (intentData != null) {
            isRateEnergy = intentData.getBoolean("rateEnergy", false);
        }
        if (isRateEnergy){
            /*Log.d(TAG, "rate energy, isRateEnergy: " + isRateEnergy);*/
            RateEnergyDialog rateEnergyDialog = new RateEnergyDialog();
            rateEnergyDialog.show(getSupportFragmentManager(), "Rate energy level");
        }

    }

    // Receive speech input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle recoded audio result
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (resultCode == RESULT_OK) {
                //  setup and show weekly craving progress bar
                cravingRecordProgr.setIndeterminate(true);
                cravingRecordProgr.setMessage("Saving weekly cravings...");
                cravingRecordProgr.show();

                // upload file to firebase storage
                String filename = UUID.randomUUID().toString();  // file name (unique random string)
                StorageReference storageRef = storage.getReference(); // ref to root bucket
                // reference to file path in the bucket
                StorageReference fileref = storageRef.child("file_dir/"
                        + uId + "/" + filename + ".wav");
                // the saved audio file path uri
                Uri file = Uri.fromFile(new File(AUDIO_FILE_PATH));
                // upload file firebase storage task
                UploadTask uploadTask = fileref.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("MainActivity", "error uploading file recorded audio clip: "
                                + exception);

                        cravingRecordProgr.dismiss();
                        Toast.makeText(MainActivity.this,
                                "Error occurred while processing audio clip",
                                Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // save uploaded file meta data on mongodb
                        String filename = taskSnapshot.getMetadata().getName();
                        Long longDate = taskSnapshot.getMetadata().getCreationTimeMillis();
                        Date creationDate = new Date(longDate);
                        saveMetaData(filename, creationDate);
                        /*Log.d("upload", "uploaded file name: " + filename);*/
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
                cravingRecordProgr.dismiss();
                Toast.makeText(MainActivity.this,
                        "Recording cancelled!", Toast.LENGTH_LONG).show();
                /*Log.d("MainActivity", "recording cancelled");*/
            }
        }
    }

    private void saveMetaData(String fileName, Date creationDate){
        Toast.makeText(MainActivity.this,
                "Audio clip processed successfully",
                Toast.LENGTH_LONG).show();

        /*Log.d("MainActivity", "saveMetaData triggered");*/

        Document metaDataDoc = new Document()
                .append("uId", uId)
                .append("filename", fileName)
                .append("fulfilled", false)
                .append("creationDate", creationDate);

        final Task <RemoteInsertOneResult> insertTask = recordingsCollection.insertOne(metaDataDoc);
        insertTask.addOnCompleteListener(new OnCompleteListener <RemoteInsertOneResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    /*Log.d("MainActivity", String.format("successfully inserted userDoc" +
                                    " with id %s", task.getResult().getInsertedId()));*/
                    startAnalysis();
                    cravingRecordProgr.dismiss();
                    cravingRecordProgr.setMessage("Starting analysis...");
                    cravingRecordProgr.show();
                    Toast.makeText(MainActivity.this, "Weekly cravings saved",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.e("metaDataDoc", "failed to insert userDoc with: ",
                            task.getException());
                    cravingRecordProgr.dismiss();
                }
            }
        });
    }

    private void startAnalysis(){
        Document filterDoc = new Document().append("uId",uId);
        Document updateDoc = new Document().append("$set",
                new Document()
                        .append("sessCompletConfrm", true)
                        .append("mealPlanGen", false)
                        .append("cravingRecdate", currDay)
        ); // set sessCompletConfrm/cravings_recorded ==  true and mealPlanGen/cravings_generated to false

        final Task<RemoteUpdateResult> updateTask = dietsCollection.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    /*long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();*/
                    /*Log.d("MainActivity", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));*/

                    // NOTIFY USER AND SET VIEWS
                    Toast.makeText(MainActivity.this, "Analysis started",
                            Toast.LENGTH_LONG).show();
                    cravingRecordProgr.dismiss();
                    mEmptyMealsNoSessSched.setVisibility(View.GONE);
                    mPreparingStateMealsView.setVisibility(View.VISIBLE);
                    mPreparingProgress.setVisibility(View.VISIBLE);
                } else {
                    Log.e("startAnalysis", "failed to update document with: ",
                            task.getException());
                }
            }
        });
    }

    private void setAsPrepedForLater(String recomndID){
        recomndsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        Document filterDoc = new Document().append("_id", new ObjectId(recomndID));
        Document updateDoc = new Document().append("$set",
                new Document()
                        .append("isPreped", true)
        );

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);

        final Task<RemoteUpdateResult> updateTask = recomndsCollection.updateOne(filterDoc, updateDoc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    /*Log.d("MainActivity", String.format("successfully matched %d and modified" +
                                    " %d documents @setAsPrepedForLater", numMatched, numModified));*/
                    // Update ui
                    UImodel.getUiData(currDay, uId);
                } else {
                    Log.e("startAnalysis", "failed to update document @setAsPrepedForLater" +
                                    " error: ", task.getException());
                }
            }
        });
    }

    private void setConsulteeSchedSess(){
        Document filterDoc = new Document().append("uId",uId);
        Document updateDoc = new Document().append("$set",
                new Document()
                        .append("consulteeSchedSess", true)
        );

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);

        final Task<RemoteUpdateResult> updateTask = dietsCollection.updateOne(filterDoc, updateDoc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    /*long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();*/
                    /*Log.d("MainActivity", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));*/

                    mPreparingStateMealsView.setVisibility(View.GONE);
                    mMealLoadingProgr.setVisibility(View.GONE);
                    mPreparingProgress.setVisibility(View.GONE);
                    scheduleSessionBtn.setVisibility(View.GONE);
                    mEmptyMealsNoSessSched.setVisibility(View.GONE);
                    expandableList.setVisibility(View.GONE);
                    upcomingSessNoMeal.setVisibility(View.VISIBLE);
                    gotToSessBtn.setVisibility(View.VISIBLE);

                    Toast.makeText(MainActivity.this, "Session scheduled", Toast.LENGTH_LONG).show();
                    // update UI
                    UImodel.getUiData(currDay, uId);
                } else {
                    Log.e("setConsulteeSchedSess", "failed to update document with: ",
                            task.getException());
                }
            }
        });
    }

    private void scheduleSession(View view){
        /*Log.d(TAG, "schedule session button clicked");*/
        // hide schedule button and test and show loading animation
        mEmptyMealsNoSessSched.setVisibility(View.GONE);
        scheduleSessionBtn.setVisibility(View.GONE);
        mMealLoadingProgr.setVisibility(View.VISIBLE);
        // Setup and show date picker dialog listener
        Calendar now = Calendar.getInstance();  // instantiate calender to get current time
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                MainActivity.this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        );
        // set accent color
        dpd.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        // Set on  cancel listener
        dpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mMealLoadingProgr.setVisibility(View.GONE);
                mEmptyMealsNoSessSched.setVisibility(View.VISIBLE);
                scheduleSessionBtn.setVisibility(View.VISIBLE);
            }
        });
        // show date picker dialog
        dpd.show(getSupportFragmentManager(), "Datepickerdialog");
    }




    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        // Set selected date
        selectedYear = year;
        selectedMonth = monthOfYear;
        selectedday = dayOfMonth;

        // Setup and show time picker dialog listener
        Calendar now = Calendar.getInstance();  // instantiate calender to get current time
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR),
                now.get(Calendar.MINUTE),
                false
        );
        // set accent color
        tpd.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        // Set on  cancel listener
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mMealLoadingProgr.setVisibility(View.GONE);
                mEmptyMealsNoSessSched.setVisibility(View.VISIBLE);
                scheduleSessionBtn.setVisibility(View.VISIBLE);
            }
        });
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
        updateSession(scheduledDate);
        Toast.makeText(this, "Scheduling your session...", Toast.LENGTH_SHORT).show();
    }

    private void updateSession(Date date){

        Document filterDoc = new Document()
                .append("consulteeId", uId)
                .append("approved", false)
                .append("completed", false)
                .append("scheduled", false)
                .append("missed", false)
                .append("cancelled", false);

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

                    // update scheduled sessions status
                    setConsulteeSchedSess();

                } else {
                    Log.e("main/updateSettings", "failed to update document with: ", task.getException());
                }
            }
        });
    }

    /*private void record(View view){
        // Very audio recoding permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
            // Very data writing to external storage permission
        }else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }else{
            // start audio recorder
            int color = getResources().getColor(R.color.colorPrimaryDark);
            AndroidAudioRecorder.with(this)
                    // Required
                    .setFilePath(AUDIO_FILE_PATH)
                    .setColor(color)
                    .setRequestCode(REQUEST_RECORD_AUDIO)

                    // Optional
                    .setSource(AudioSource.MIC)
                    .setChannel(AudioChannel.STEREO)
                    .setSampleRate(AudioSampleRate.HZ_48000)
                    .setAutoStart(true)
                    .setKeepDisplayOn(true)

                    // Start recording
                    .record();
        }
    }*/

    /*private void showTextView(String message, int type) {
        amani.setTextColor(Color.WHITE);
        if (amani.getLineCount() > 3){
            scroll_indic.setVisibility(View.VISIBLE);
        }
        amani.setFocusableInTouchMode(true);
        amani.setText(message);
        amani.requestFocus();
        scroll_indic.setVisibility(View.GONE);
        IndicatorAnim.cancel();
        if (amani.isVerticalScrollBarEnabled()){
            scrollIndicBlinkEffect(IndicatorAnim);
        }
        foodInput.requestFocus(); // change focus back to edit text to continue typing
    }*/

    /*private void scrollIndicBlinkEffect(Animation anim) {
        anim.setDuration(1000); //1 second duration for each animation cycle
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE); //repeating indefinitely
        anim.setRepeatMode(Animation.REVERSE); //animation will start from end point once ended.
        scroll_indic.setVisibility(View.VISIBLE);
        scroll_indic.startAnimation(anim); //to start animation
    }*/

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

    /*public class CustomTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    // Action you you want on finger down.
                    scrollToButtom();
                    IndicatorAnim.cancel();
                    scroll_indic.setVisibility(View.GONE);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    // Action you you want on finger up
                    break;
            }
            return false;
        }
    }*/
    // get screen dimension changes to detect keyboard presence on the screen
    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
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
                FeedbackDialog feedBackDialog = new FeedbackDialog();
                feedBackDialog.show(getSupportFragmentManager(), "feedBack");
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


    /*public void scrollToButtom() {

        int x = 0;
        int y = amani.getMaxHeight();

        ObjectAnimator xTranslate = ObjectAnimator.ofInt(amani, "scrollX", x);
        ObjectAnimator yTranslate = ObjectAnimator.ofInt(amani, "scrollY", y);

        AnimatorSet animators = new AnimatorSet();
        animators.setDuration(1500L);
        animators.playTogether(xTranslate, yTranslate);

        animators.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onAnimationEnd(Animator arg0) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onAnimationCancel(Animator arg0) {
                // TODO Auto-generated method stub
            }
        });
        animators.start();
    }*/

    private boolean chkAuth(){
        // Check if user is signed in (true). //mongoDB
        if (!stitchClient.getAuth().isLoggedIn()) {
            finish();
            //send user to login layout
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return  false;
        }else{
            // check intro is hsown and check subscription
            showIntroAndSubchk();
            // verify subscription with google play billing
            /*if(validSub()){
                Log.d(TAG, "User subscription is valid");
            }else{
                Log.d(TAG, "User subscription is invalid");
            }*/
            if(user != null){
                updatetoken();
                uId = user.getId();
                /*Log.d("MainActivity", "user Id: " + uId);*/
                email = user.getProfile().getEmail();
                profileImgUrl = user.getProfile().getPictureUrl();
                return  true;
            }else{
                Log.e("MainActivity", "Iser is authenticated but user value is null");
                stitchClient.getAuth().logout(); // log the user out
                //send user to login layout
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return  false;
            }
        }
    }

   // updates ui with queried data from user diet and daily macros
   private void setUiData(Document dietDoc, Document dailyMacDoc, ArrayList<RecomndRecipe> recomndRecipes){

       if (dietDoc != null && dailyMacDoc != null) {
           Diet diet = new Diet(dietDoc);
           DailyMac dailyMac = new DailyMac(dailyMacDoc);

           try {
               int protPerc = (int) Math.round((dailyMac.getProt() / diet.getProt()) * 100);
               /*Log.d("setUiData", "protPerc dailyMac.getProt: " + dailyMac.getProt());
               Log.d("setUiData", "protPerc diet.getProt: " + diet.getProt());*/
               int fatPerc = (int) Math.round((dailyMac.getFat() / diet.getFat()) * 100);
               /*Log.d("setUiData", "fatPerc dailyMac.fatPerc: " + dailyMac.getFat());
               Log.d("setUiData", "fatPerc diet.fatPerc: " + diet.getFat());*/
               int carbsoPerc = (int) Math.round((dailyMac.getCarbs() / diet.getCarbs()) * 100);
               /*Log.d("setUiData", "carbsoPerc dailyMac.getCarbs: " + dailyMac.getCarbs());
               Log.d("setUiData", "carbsoPerc diet.getCarbs: " + diet.getCarbs());*/
               int calPerc = (int) Math.round((dailyMac.getCal() / diet.getCal()) * 100);
               /*Log.d("setUiData", "calPerc dailyMac.getCal: " + dailyMac.getCal());
               Log.d("setUiData", "calPerc diet.getCal: " + diet.getCal());*/

               mprog_prot.setProgress(100, true); // initialize animation
               mprog_fat.setProgress(100, true); // initialize animation
               mprog_carbs.setProgress(100, true); // initialize animation
               /*mprog_prot.setProgress(100, true);*/
               // delay roll back to original progress value
               new android.os.Handler().postDelayed(
                       new Runnable() {
                           public void run() {
                               mprog_prot.setProgress(Math.round(protPerc), true); // set calculated val
                               mprog_fat.setProgress(Math.round(fatPerc), true); // set calculated val
                               mprog_carbs.setProgress(Math.round(carbsoPerc), true); // set calculated val
                               mTotalProg.setProgress(calPerc); // // set calculated calories/total macros
                           }
                       }, 1500);
           } catch (Exception e) {
               Log.e(TAG, "Error calculating stats: " + e);
           }
       }


       // check if elements in the list is not null (while list size might be  > 0,
       // elements iin the list could be null )
       String recomnd;
       try{
           if (recomndRecipes.size() != 0) {
               /*Log.d("mainActivity", "recomndRecipes: " + recomndRecipes.toString());*/
               recomnd = recomndRecipes.get(0).getRecomnd();
           }else{
               recomnd = null;
               Log.e("mainActivity", "recomndRecipes: " + recomndRecipes.toString());
           }
       }catch(Exception e){
           recomnd = null;
       }

       if (recomnd != null){
           // cAdd data to lists
           listHeaders = new ArrayList<String>();
           ListsData = new HashMap<String, ArrayList<RecomndRecipe>>();
           ArrayList<RecomndRecipe> breakfastRecmnds = new ArrayList<RecomndRecipe>();
           ArrayList<RecomndRecipe> lunchRecmnds = new ArrayList<RecomndRecipe>();
           ArrayList<RecomndRecipe> dinnerRecmnds = new ArrayList<RecomndRecipe>();
           ArrayList<RecomndRecipe> snacksRecmnds = new ArrayList<RecomndRecipe>();

           listHeaders.add("BREAKFAST");
           listHeaders.add("LUNCH");
           listHeaders.add("DINNER");
           listHeaders.add("SNACKS");

           for(RecomndRecipe recomndRecipe: recomndRecipes){
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
           recomndListAdapter = new RecomndsAdapter(MainActivity.this, listHeaders, ListsData);

           // set adapter to list
           expandableList.setAdapter(recomndListAdapter);


           /*Log.d("mainActivity","recommends set ot adapter: " + recomndRecipes.toString());*/
           expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
               @Override
               public boolean onChildClick(ExpandableListView expandableListView, View view,
                                           int groupPosition, int childPosition, long l) {
                   // set recipe dialog and listener
                   MealRecipeDialog mealRecipeDialog = new MealRecipeDialog(
                           MainActivity.this, R.style.AppTheme);

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
                       mealRecipeDialog.setOwnerActivity(MainActivity.this);
                       backState = false;
                       showMealRecipeDialog(mealRecipeDialog);
                   }else{
                       ConfirmEatenDialog confirmEatenDialog = new ConfirmEatenDialog();
                       confirmEatenDialog.setmRecomndRecipe(clickedRecipe); //set recomndId of item clicked
                       confirmEatenDialog.show(getSupportFragmentManager(), "Confirm eaten");
                   }
                   return false;
               }
           });
           // Set UI
           mMealLoadingProgr.setVisibility(View.GONE);
           expandableList.setVisibility(View.VISIBLE);

       }else{
           //Log.d(TAG, "recommend is null");
           mMealLoadingProgr.setVisibility(View.VISIBLE);
           fetchMealPlanStatus();
       }
   }

   // fetch user weekly cravings status
   private void fetchMealPlanStatus(){
       //Log.d("fetchMealPlanStatus", "Triggered");
       Task <Document> findTask = dietsCollection.find().limit(1).first();
       findTask.addOnCompleteListener(new OnCompleteListener <Document> () {
           @Override
           public void onComplete(@NonNull Task <Document> task) {
               if (task.isSuccessful()) {
                   if (task.getResult() == null) {
                       Log.e("fetchMealPlanStatus",
                               "Could not find any matching diet document to get cravings status");
                   } else {
                        /*Log.d("MainActivity", String.format("successfully found diet doc " +
                                        "for cravings status status: %s",
                                task.getResult().toString()));*/
                       boolean sessCompletConfrm;
                       boolean mealPlanGen;
                       boolean consulteeSchedSess;
                       try {
                           sessCompletConfrm = task.getResult().getBoolean("sessCompletConfrm");
                       }catch (Exception e){
                           sessCompletConfrm = false;
                           Log.e("Mainactivity", "error retrieving cravings status");
                       }
                       try {
                           mealPlanGen = task.getResult().getBoolean("mealPlanGen");
                       }catch (Exception e){
                           mealPlanGen = false;
                           Log.e("Mainactivity", "error retrieving cravings status");
                       }
                       try {
                           consulteeSchedSess = task.getResult().getBoolean("consulteeSchedSess");
                       }catch (Exception e){
                           consulteeSchedSess = false;
                           Log.e("Mainactivity", "error retrieving cravings status");
                       }
                       /*Log.d(TAG, "sessCompletConfrm: " + sessCompletConfrm +
                                   " mealPlanGen: " + mealPlanGen + " consulteeSchedSess: "
                                   + consulteeSchedSess);*/
                       // if cravings is set but not generated show preparing_meal_plan progress bar
                       if (sessCompletConfrm && mealPlanGen && consulteeSchedSess){
                           //Log.d(TAG, "First meal plan list condition triggered");
                           /*amani.setText(R.string.amani_meals_aval);*/
                           mEmptyMealsNoSessSched.setVisibility(View.GONE);
                           mPreparingProgress.setVisibility(View.GONE);
                           mPreparingStateMealsView.setVisibility(View.GONE);
                           scheduleSessionBtn.setVisibility(View.GONE);
                           upcomingSessNoMeal.setVisibility(View.GONE);
                           mMealLoadingProgr.setVisibility(View.GONE);
                           gotToSessBtn.setVisibility(View.GONE);
                           // check if recipes found
                           if (recomndListAdapter != null) {
                               expandableList.setVisibility(View.VISIBLE);
                           }else{
                               expandableList.setVisibility(View.GONE);
                               upcomingSessNoMeal.setVisibility(View.VISIBLE);
                               gotToSessBtn.setVisibility(View.VISIBLE);
                           }
                       }
                       else if (sessCompletConfrm && mealPlanGen && !consulteeSchedSess){
                           //Log.d(TAG, "true, true, false. triggered");
                           /*amani.setText(R.string.amani_meals_aval);*/
                           mEmptyMealsNoSessSched.setVisibility(View.GONE);
                           mPreparingProgress.setVisibility(View.GONE);
                           mPreparingStateMealsView.setVisibility(View.GONE);
                           scheduleSessionBtn.setVisibility(View.GONE);
                           upcomingSessNoMeal.setVisibility(View.GONE);
                           mMealLoadingProgr.setVisibility(View.GONE);
                           gotToSessBtn.setVisibility(View.GONE);
                           // check if recipes found
                           if (recomndListAdapter != null) {
                               expandableList.setVisibility(View.VISIBLE);
                           }else{
                               expandableList.setVisibility(View.GONE);
                               mEmptyMealsNoSessSched.setVisibility(View.VISIBLE);
                               scheduleSessionBtn.setVisibility(View.VISIBLE);
                           }
                       }
                       else if (sessCompletConfrm && !mealPlanGen && consulteeSchedSess){
                           /*Log.d(TAG, "sessCompletConfrm: " + sessCompletConfrm +
                                   " mealPlanGen: " + mealPlanGen + " consulteeSchedSess: "
                                   + consulteeSchedSess);*/
                           /*amani.setText(R.string.amani_meals_prep);*/
                           mMealLoadingProgr.setVisibility(View.GONE);
                           mEmptyMealsNoSessSched.setVisibility(View.GONE);
                           scheduleSessionBtn.setVisibility(View.GONE);
                           expandableList.setVisibility(View.GONE);
                           upcomingSessNoMeal.setVisibility(View.GONE);
                           gotToSessBtn.setVisibility(View.GONE);
                           mPreparingProgress.setVisibility(View.VISIBLE);
                           mPreparingStateMealsView.setVisibility(View.VISIBLE);
                       }
                       else if (sessCompletConfrm && !mealPlanGen && !consulteeSchedSess){
                           /*Log.d(TAG, "sessCompletConfrm: " + sessCompletConfrm +
                                   " mealPlanGen: " + mealPlanGen + " consulteeSchedSess: "
                                   + consulteeSchedSess);*/
                           /*amani.setText(R.string.amani_meals_prep);*/
                           mMealLoadingProgr.setVisibility(View.GONE);
                           mEmptyMealsNoSessSched.setVisibility(View.GONE);
                           scheduleSessionBtn.setVisibility(View.GONE);
                           expandableList.setVisibility(View.GONE);
                           upcomingSessNoMeal.setVisibility(View.GONE);
                           gotToSessBtn.setVisibility(View.GONE);
                           mPreparingProgress.setVisibility(View.VISIBLE);
                           mPreparingStateMealsView.setVisibility(View.VISIBLE);
                       }
                       else if (!sessCompletConfrm && !mealPlanGen && consulteeSchedSess){
                          /* Log.d(TAG, "sessCompletConfrm: " + sessCompletConfrm +
                                   " mealPlanGen: " + mealPlanGen + " consulteeSchedSess: "
                                   + consulteeSchedSess);*/
                           /*amani.setText(R.string.amani_meals_prep);*/
                           mMealLoadingProgr.setVisibility(View.GONE);
                           mEmptyMealsNoSessSched.setVisibility(View.GONE);
                           mPreparingProgress.setVisibility(View.GONE);
                           mPreparingStateMealsView.setVisibility(View.GONE);
                           scheduleSessionBtn.setVisibility(View.GONE);
                           expandableList.setVisibility(View.GONE);
                           upcomingSessNoMeal.setVisibility(View.VISIBLE);
                           gotToSessBtn.setVisibility(View.VISIBLE);
                       }
                       else if (!sessCompletConfrm && mealPlanGen && !consulteeSchedSess){
                           /*Log.d(TAG, "sessCompletConfrm: " + sessCompletConfrm +
                                   " mealPlanGen: " + mealPlanGen + " consulteeSchedSess: "
                                   + consulteeSchedSess);*/
                           /*amani.setText(R.string.amani_meals_prep);*/
                           mMealLoadingProgr.setVisibility(View.GONE);
                           mEmptyMealsNoSessSched.setVisibility(View.GONE);
                           mPreparingProgress.setVisibility(View.GONE);
                           mPreparingStateMealsView.setVisibility(View.GONE);
                           scheduleSessionBtn.setVisibility(View.GONE);
                           upcomingSessNoMeal.setVisibility(View.GONE);
                           gotToSessBtn.setVisibility(View.GONE);
                           // check if recipes found
                           if (recomndListAdapter != null) {
                               expandableList.setVisibility(View.VISIBLE);
                           }else{
                               expandableList.setVisibility(View.GONE);
                               mEmptyMealsNoSessSched.setVisibility(View.VISIBLE);
                               scheduleSessionBtn.setVisibility(View.VISIBLE);
                           }
                       }
                       else if (!sessCompletConfrm && !mealPlanGen && !consulteeSchedSess){
                           /*Log.d(TAG, "sessCompletConfrm: " + sessCompletConfrm +
                                   " mealPlanGen: " + mealPlanGen + " consulteeSchedSess: "
                                   + consulteeSchedSess);*/
                           /*amani.setText(R.string.amani_meals_prep);*/
                           mMealLoadingProgr.setVisibility(View.GONE);
                           mPreparingProgress.setVisibility(View.GONE);
                           mPreparingStateMealsView.setVisibility(View.GONE);
                           upcomingSessNoMeal.setVisibility(View.GONE);
                           expandableList.setVisibility(View.GONE);
                           gotToSessBtn.setVisibility(View.GONE);
                           mEmptyMealsNoSessSched.setVisibility(View.VISIBLE);
                           scheduleSessionBtn.setVisibility(View.VISIBLE);
                       }else{
                           Log.e("MainActivity", "failed to match any of meal plans" +
                                   " filtering conditions");
                       }
                   }
               } else {
                       Log.e("MainActivity", "failed to find diet document for fetchMealPlanStatus() with: ", task.getException());
               }
           }
       });
   }

    // create user document and diet document
    private void createUserDoc(String email, String userId, Double activityModifier,
                               int age, String gender, String goal, Double height, Double weight){
        //Log.d("createUserDoc", "createUserDoc triggered");
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Preparing account...");
        progressDialog.show();

        Document filterDoc = new Document("uId", userId);

        Document userDoc = new Document()
                .append("$set", new Document()
                        .append("uId", userId)
                        .append("email", email)
                        .append("activity_modifier", activityModifier)
                        .append("age", age)
                        .append("gender", gender)
                        .append("goal", goal)
                        .append("height", height)
                        .append("weight", weight)
                        .append("dietCreated", false)
                        .append("dietTz", Calendar.getInstance().getTimeZone().getID())
                        .append("messagingToken", mToken)
                        .append("tokenDate", currDay)
                        .append("lastModified", Calendar.getInstance().getTime()));

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);

        final Task <RemoteUpdateResult> updateTask = usersCollection.updateOne(
                filterDoc, userDoc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    /*Log.d("createUserDoc", String.format("successfully inserted/updated userDoc" +
                                    " with id %s", task.getResult().getInsertedId()));*/
                    progressDialog.dismiss();
                    createDietDoc();
                } else {
                    Log.e("MainActivity", "failed to insert/updating userDoc with: ",
                            task.getException());
                    progressDialog.dismiss();
                }
            }
        });
    }

    // create diet document
    private void createDietDoc(){
        /*Log.d("createDietDoc", "createDietDoc triggered");*/
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Preparing diet...");
        progressDialog.show();

        Document filterDoc = new Document("uId", uId);

        Document dietDoc = new Document()
                .append("uId", uId)
                .append("sessCompletConfrm", false)
                .append("mealPlanGen", false)
                .append("consulteeSchedSess", false)
                .append("lastModified", Calendar.getInstance().getTime());

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);

        final Task <RemoteUpdateResult> updateTask = dietsCollection.updateOne(
                filterDoc, dietDoc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    /*Log.d("createDietDoc", String.format("successfully inserted/updated dietDoc" +
                                    " with id %s", task.getResult().getUpsertedId()));*/
                    try {
                        getDietID(); // get diet doc id snd and it to user Doc
                    }catch (Exception e){
                        Log.e("MainActivity", "Error updating user doc with diet id");
                    }
                    progressDialog.dismiss();
                } else {
                    Log.e("createDietDoc", "failed to insert/updating dietDoc with: ",
                            task.getException());
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void getDietID(){

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Preparing account...");
        progressDialog.show();

        dietsCollection = mongoDBClient.getDatabase("activium_app")
                .getCollection("diets");
        Document filterDoc = new Document().append("uId", uId);
        Task <Document> findTask = dietsCollection.find(filterDoc).limit(1).first();
        findTask.addOnCompleteListener(new OnCompleteListener <Document> () {
            @Override
            public void onComplete(@NonNull Task <Document> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null) {
                        Log.e("MainActivity",
                                "Could not find diet document" + task.getResult().toString());
                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        updateUserDocwithDietID(task.getResult().getObjectId("_id").toString());
                    }
                } else {
                    Log.e("MainActivity", "failed to find diet document with: ", task.getException());
                    progressDialog.dismiss();
                }
            }
        });

    }

    private void updateUserDocwithDietID(String dietId){

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Preparing account...");
        progressDialog.show();

        Document filterDoc = new Document("uId", uId);
        Document updateDoc = new Document()
                .append("$set", new Document()
                .append("dietId", dietId)
                );

        final Task<RemoteUpdateResult> updateTask =
                usersCollection.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener<RemoteUpdateResult>() {
            @Override
            public void onComplete(@NonNull Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    /*long numModified = task.getResult().getModifiedCount();*/
                    /*Log.d("MainActivity", "Successfully match user document." +
                            " no. matched: " + numModified);*/
                    createSettings();
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    Log.e("mainActivity", "failed to update document with: ", task.getException());
                }
            }
        });
    }



    private void createSettings(){
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Preparing account...");
        progressDialog.show();

        Document filterDoc = new Document("uId",uId);

        // Create user setting document
        Document userSettings = new Document()
                .append("macro_logging_rmnd",
                        new Document()
                                .append("state", true)
                                .append("frequency", 3))
                .append("diet_status_rmnd",
                        new Document()
                                .append("state", true)
                                .append("frequency", 3))
                .append("audio",true)
                .append("lastModified", Calendar.getInstance().getTime())
                .append("uId",uId);

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);

        final Task <RemoteUpdateResult> updateTask = settingsCollection.updateOne(
                filterDoc, userSettings, options);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    /*Log.d("MainActivity", String.format("successfully inserted userSettingd" +
                                    " with id %s", task.getResult().getInsertedId()));*/

                    chkPoint = getSharedPreferences("chkPoint", MODE_PRIVATE);
                    updateChkPoint(); // set document initiation to False

                    // TEMPORARY
                    try{
                        // clear pseudoCached login data
                        SharedPreferences.Editor editor = pseudoCache.edit();
                        editor.remove("lastUserPass");
                        editor.apply();
                        /*Log.e("removeTempData", "removed pseudoCached data successfully!");*/
                    }catch(Exception e){
                        Log.e("removeTempData", "error removing pseudoCached data: " + e);
                    }

                    // handle new setup
                    makeServerCall(uId, "new_setup", null, null,
                            null, null, null, 0);

                    UImodel.getUiData(currDay, uId);
                    progressDialog.dismiss();
                } else {
                    /*Log.e("createSettings", "failed to userSettingd document with: ",
                            task.getException());*/
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void updateSettings() {
        boolean macLogState = settings.getBoolean("macro_logging_state", false);
        int macLogInt = settings.getInt("macro_logging_int", 0);
        boolean dtState = settings.getBoolean("diet_status_state", false);
        int dtInt = settings.getInt("diet_status_int", 0);
        boolean audio = settings.getBoolean("audio", false);

        Document filterDoc = new Document();
        Document updateDoc = new Document().append("$set", new Document()
                .append("macro_logging_rmnd",
                        new Document()
                                .append("state", macLogState)
                                .append("frequency", macLogInt))
                .append("diet_status_rmnd",
                        new Document()
                                .append("state", dtState)
                                .append("frequency", dtInt))
                .append("audio", audio)
                .append("lastModified", Calendar.getInstance().getTime()));

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);

        final Task<RemoteUpdateResult> updateTask =
                settingsCollection.updateOne(filterDoc, updateDoc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener<RemoteUpdateResult>() {
            @Override
            public void onComplete(@NonNull Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numModified = task.getResult().getModifiedCount();
                    /*Log.d("main/updateSettings", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));*/
                    if (numModified == 1){
                        settingsEditor.putBoolean("syncState", true);
                        settingsEditor.apply();
                    }
                } else {
                    Log.e("main/updateSettings", "failed to update document with: ", task.getException());
                }
            }
        });
    }

    public void showUserInfoDialog() {
        UserInfoDialog userInfoDialog = new UserInfoDialog();
        userInfoDialog.show(getSupportFragmentManager(), "user_info");
    }

    public void showMealRecipeDialog(MealRecipeDialog mealRecipeDialog) {
        mealRecipeDialog.show();
    }

    @Override
    public void createUserDoc(Double activityModifier, int age, String gender, String goal,
                              Double height, Double weight) {
        createUserDoc(email, uId, activityModifier, age, gender, goal, height, weight);
        /*Log.d("createUserDoc", "age: "  + age + "gender: " + gender +
                "height: " + height + "weight: " + weight);*/
    }

    // Query backend server
    public void makeServerCall(String uId, String intent, String food, String measure,
                               Double quantity, String recipeId, String recomndId, int dayHour) {
        /*Log.d("MainActiviy", "fetchEarthquakeData initialized");*/

        // Create URL object
        URL url = makeUrl(uri);

        // Perform HTTP request to the URL and receive a JSON response back
        try {
            makeHTTPRequest(url, uId, intent, food, measure, quantity, recipeId, recomndId, dayHour);
        } catch (IOException e) {
            Log.e("MainActiviy", "Error making Http request to server", e);
        }

    }

    public static String extractResponse (String jsonResponse) {
        String response =  null;
        // If the JSON string is empty or null, then return null value.
        if (!TextUtils.isEmpty(jsonResponse)) {
            // Try parsing the json response
            try {
                String simpleJsonString = jsonResponse;
                JSON json = new JSON(simpleJsonString);
                response = json.getJsonObject().getString("fulfillmentText");
            } catch (Exception e) {
                Log.e("MainActiviy", "Problem parsing the JSON results", e);
            }
        }
        return response;
    }

    private static URL makeUrl(String urlString){
        URL url = null;
        try {
            url = new URL(urlString);
        }catch(MalformedURLException e){
            Log.e("MainActiviy", "Problem making URL object from string", e);
        }
        return url;
    }

    private void makeHTTPRequest(URL url,String uId, String intent, String food, String measure,
                                 Double quantity, String recipeId,String recomndId, int dayHour)
            throws IOException {
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
                String jsonString = "{\"request\": \"\"}";

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
                UImodel.getUiData(currDay, uId);
                if (responseString != null){
                    Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                }
                /*Log.d("MainActiviy", "server response: " + responseString);*/
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

    /*private void setText(String jsonResponse){
        amani.setText(extractResponse(jsonResponse));
        // set response text
        spin_kit.setVisibility(View.GONE);
        amani.setVisibility(View.VISIBLE);
        if (amani.getLineCount() > 3){
            scroll_indic.setVisibility(View.VISIBLE);
        }
    }*/

    private void updatetoken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        mToken = token;
                        // Log and toast
                        /*Log.d(TAG, "firebase messaging token: " + token);*/
                    }
                });
    }

   /* private void notifyMaster(String cravingID){
        String to = getString(R.string.master_notif_key); // the notification key
        AtomicInteger msgId = new AtomicInteger();
        FirebaseMessagingService.getInstance().send(new RemoteMessage.Builder(to)
                .setMessageId(String.valueOf(msgId.get()))
                .addData("creavingId", cravingID)
                .build());
    }*/

    @Override
    public void setEaten(RecomndRecipe recomndRecipe) {
        // show loading and hide list
        expandableList.setVisibility(View.GONE);
        mMealLoadingProgr.setVisibility(View.VISIBLE);
        //remove recipe from list
        /*if(recomndRecipe.getIsBreakfast()){
            recomndListAdapter.remove(recomndRecipe);
        }*/
        // get current hour of the day
        SimpleDateFormat formatter = new SimpleDateFormat("HH");
        Date date = Calendar.getInstance().getTime();
        int hour = Integer.valueOf(formatter.format(date));
        //enable back button
        backState = true;
        // log eaten meal with backend call
        makeServerCall(user.getId(), "log_meal", null, null, null,
                recomndRecipe.getRecipeId(), recomndRecipe.getRecomndId(), hour);
    }

    @Override
    public void setPrepForLater(RecomndRecipe recomndRecipe) {
        // show loading and hide list
        expandableList.setVisibility(View.GONE);
        mMealLoadingProgr.setVisibility(View.VISIBLE);
        // mark recommended recipe as preped for later
        setAsPrepedForLater(recomndRecipe.getRecomndId());
    }

    private void verifySubscription(){
        if(isConnected) {
            subscriptionsCollection = mongoDBClient.getDatabase("database_name")
                    .getCollection("collection_name");
            Document filterDoc = new Document().append("uId", uId);
            Task<Document> findTask = subscriptionsCollection.find(filterDoc).limit(1).first();
            findTask.addOnCompleteListener(new OnCompleteListener<Document>() {
                @Override
                public void onComplete(@NonNull Task<Document> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() == null) {
                            Log.e("MainActivity",
                                    "Could not find any matching subscription document to get" +
                                            " subscription status for user: " + uId);
                            // if verification is attempted 5 times
                            // send to subscription activity on the 6th time
                            if (subVeriftAtmp == 6){
                                // send to subscription screen
                                finish();
                                startActivity(new Intent(MainActivity.this, SubscriptionActivity.class));
                            }else{
                                // increase attempt count and try again
                                subVeriftAtmp += 1;
                                verifySubscription();
                            }
                        } else {
                            Date subExpiry = task.getResult().getDate("subexpiry");
                            Boolean valid = task.getResult().getBoolean("valid", false);// subscription validity (not cancelled)
                            if (subExpiry != null && valid) {
                                if (subExpiry.compareTo(currDay) < 0) {
                                    /*Log.d("subchk", "subscription invalid, subExpiry:" +
                                            " " + subExpiry + " currDay: " + currDay);*/
                                    // send to subscription screen
                                    finish();
                                    startActivity(new Intent(MainActivity.this, SubscriptionActivity.class));
                                }
                            } else {
                                Log.e("MainActivity",
                                        "Eroor subExpiry: " + subExpiry + "validity: " + valid);
                            }
                        }
                    } else {
                        Log.e("MainActivity", "failed to find subscription document with: ", task.getException());
                        // if verification is attempted 5 times
                        // send to subscription activity on the 6th time
                        if (subVeriftAtmp == 6){
                            // send to subscription screen
                            finish();
                            startActivity(new Intent(MainActivity.this, SubscriptionActivity.class));
                        }else{
                            // increase attempt count and try again
                            subVeriftAtmp += 1;
                            verifySubscription();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void triggerSuccessToast() {
        Toast.makeText(MainActivity.this, "FeedBack Sent. Thank you",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFailureToast() {
        Toast.makeText(MainActivity.this, "Failed sending feedback!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboard() {
        hideSoftKeyboard(MainActivity.this);
    }

    private void updateChkPoint(){
        try{
            // clear pseudoCached login data
            SharedPreferences.Editor chkPointEditor = chkPoint.edit();
            chkPointEditor.putBoolean("initDoc", false);
            chkPointEditor.apply();
            //Log.d("updateChkPoint", "updated chkPoint data successfully!");
        }catch(Exception e){
            Log.e("updateChkPoint", "error removing chkPoint data: " + e);
        }
    }

    private void showIntroAndSubchk(){
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                chkPoint = getSharedPreferences("chkPoint", MODE_PRIVATE);
                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = chkPoint.getBoolean("firstStart", true);
                /*Log.d("MainActivty", "isFirstStart: " + isFirstStart);*/
                //  If the activity has never started before...
                if (isFirstStart) {
                    //  Launch app intro
                    final Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            /*Log.d("MainActivty", "starting intro");*/
                            startActivity(i);
                        }
                    });
                    //  Make a new preferences editor
                    SharedPreferences.Editor e = chkPoint.edit();
                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);
                    //  Apply changes
                    e.apply();
                }else{
                    // verify user is subscribed
                    verifySubscription();
                    // after user went through tour check if init docs are created and user info is collected
                    boolean initdoc = chkPoint.getBoolean("initDoc", true); // get initial document initiation status
                    /*Log.d("MainActivty", "initdoc: " + initdoc);*/
                    if(initdoc){
                        // if initial docs not created initiate process by collecting user info
                        showUserInfoDialog();
                    }
                }
            }
        });
        // Start the thread
        t.start();
    }

    @Override
    public void confirmEaten(RecomndRecipe recomndRecipe) {
        // show loading and hide list
        expandableList.setVisibility(View.GONE);
        mMealLoadingProgr.setVisibility(View.VISIBLE);
        //remove recipe from list
        /*if(recomndRecipe.getIsBreakfast()){
            recomndListAdapter.remove(recomndRecipe);
        }*/
        // get current hour of the day
        SimpleDateFormat formatter = new SimpleDateFormat("HH");
        Date date = Calendar.getInstance().getTime();
        int hour = Integer.valueOf(formatter.format(date));
        //enable back button
        backState = true;
        // log eaten meal with backend call
        makeServerCall(user.getId(), "log_meal", null, null, null,
                recomndRecipe.getRecipeId(), recomndRecipe.getRecomndId(), hour);
    }

    @Override
    public void rateEnergy(int rating) {
        mEnergyRating = rating;
        RateHungerDialog hungerDialog = new RateHungerDialog();
        hungerDialog.show(getSupportFragmentManager(), "Rate hunger level");
    }

    @Override
    public void rateHunger(int HungerRating) {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Saving updates...");
        progressDialog.show();

        energyHungerCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        Document metaDataDoc = new Document()
                .append("uId", uId)
                .append("energyLevel", mEnergyRating)
                .append("hungerLevel", HungerRating)
                .append("timestamp", Calendar.getInstance().getTime());

        final Task <RemoteInsertOneResult> insertTask = energyHungerCollection.insertOne(metaDataDoc);
        insertTask.addOnCompleteListener(new OnCompleteListener <RemoteInsertOneResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    /*Log.d("MainActivity", String.format("successfully inserted energyHungerCollection Doc" +
                                    " with id %s", task.getResult().getInsertedId()));*/
                    Toast.makeText(MainActivity.this, "Update successful",
                            Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    finish();
                } else {
                    Log.e("energyHunger", "failed to insert energyHungerCollection Doc" +
                                    " with: ", task.getException());
                    Toast.makeText(MainActivity.this, "update Failed!",
                            Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    finish();
                }
            }
        });
    }

    @Override
    public void detailsSuccessUpdate() {
        Toast.makeText(MainActivity.this, "Details updated successfully",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void detailsFailureUpdate() {
        Toast.makeText(MainActivity.this, "Failed updating details!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboardUpdate() {
        hideSoftKeyboard(MainActivity.this);
    }

    private void goToSess(View v){
        Intent goToSessIntent = new Intent(MainActivity.this, ConsultationActivity.class);
        startActivity(goToSessIntent);
    }

    /*private boolean validSub(){
        billingClient = BillingClient.newBuilder(MainActivity.this).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
                Log.d(TAG, "Purchase update triggered");
            }
        }).build();
        Purchase.PurchasesResult result = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        //Log.d(TAG, "result response code : " + result.getResponseCode());
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK){
            List<Purchase> purchases = result.getPurchasesList();
            //Log.d(TAG, "purchases found: " + purchases.toString());
            for (Purchase purchase : purchases){
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                    //Log.d(TAG, "purchase verified: " + purchase.getOriginalJson());
                    return true;
                }
            }
        }
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                //Log.d(TAG, "result response code @sync: " + billingResult.getResponseCode());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    //Log.d(TAG, "purchases found @sync: " + purchaseHistoryRecordList.toString());
                    for (PurchaseHistoryRecord purchase : purchaseHistoryRecordList){
                        //Log.d(TAG, "purchase verified @sync: " + purchase.getOriginalJson());

                    }
                }
            }
        });
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                //Log.d(TAG, "result response code @setupFinished: " + result.getResponseCode());
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    List<Purchase> purchases = result.getPurchasesList();
                    //Log.d(TAG, "purchases found @setupFinished: " + purchases.toString());
                    for (Purchase purchase : purchases){
                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                            //Log.d(TAG, "purchase verified @setupFinished: " + purchase.getOriginalJson());
                        }
                    }
                }

            }
            @Override
            public void onBillingServiceDisconnected() {
                //Log.d(TAG, "service disconnected");
            }
        });
        return false;
    }*/
}






