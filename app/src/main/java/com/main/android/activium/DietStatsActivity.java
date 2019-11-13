package com.main.android.activium;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Typeface;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.navigation.NavigationView;
import com.main.android.activium.DataClasses.StatsData;
import com.main.android.activium.dialogs.FeedbackDialog;
import com.main.android.activium.ViewModels.StatsViewModel;
import com.main.android.activium.dialogs.UpdateDetailsDialog;
import com.main.android.activium.util.DailyMacChart;
import com.main.android.activium.util.DayAxisValueFormatter;
import com.main.android.activium.util.XYMarkerView;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.and;

public class DietStatsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnChartValueSelectedListener,
        com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener,
        FeedbackDialog.InterFeedBack, UpdateDetailsDialog.UpdateDetailsInterface {
    private static final String TAG = "Diet Stats Activity";

    private BarChart chart;
    private DrawerLayout drawer;
    private Toolbar toolBar;


    private Intent intent;
    // shareIntent intent for app sharing
    private Intent shareIntent;


    protected Typeface tfLight;
    private Button setFrom;
    private Button setTo;
    private TextView fromText;
    private TextView toText;
    private TextView avgTarDays;
    private TextView avgTarProt;
    private TextView avgTarCarbs;
    private TextView avgTarFat;
    private Date fromDate;
    private Date toDate;

    // MangoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    final StitchUser user = stitchClient.getAuth().getUser();

    // Drawer variables
    private String uId;
    private String email;
    private TextView userEmail; // drawer user email txt view
    private ImageView profilePhoto; // drawer user profile photo
    private String profileImgUrl;
    private NavigationView navigationView;
    private View headerView;

    // View Model
    private StatsViewModel statsModel;



    @Override
    protected void onStart() {
        super.onStart();
        // Verify user is authenticated
        chkAuth();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_stats);

        // Verify user is authenticated
        chkAuth();

        if(user != null){
            uId = user.getId();
            /*Log.d("MainActivity", "user Id: " + uId);*/
            email = user.getProfile().getEmail();
            profileImgUrl = user.getProfile().getPictureUrl();
        }

        // link variables
        drawer = findViewById(R.id.dietStats_layout);
        toolBar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view_diet_stats);
        setFrom = findViewById(R.id.from_btn);
        setTo = findViewById(R.id.to_btn);
        fromText = findViewById(R.id.from_date);
        toText = findViewById(R.id.to_date);
        avgTarDays = findViewById(R.id.avg_tgt_days);
        avgTarProt = findViewById(R.id.avg_tgt_proto);
        avgTarCarbs = findViewById(R.id.avg_tgt_carbs);
        avgTarFat = findViewById(R.id.avg_tgt_fat);
        chart = findViewById(R.id.chart1);
        headerView = navigationView.getHeaderView(0);
        userEmail = headerView.findViewById(R.id.user_email);
        profilePhoto = headerView.findViewById(R.id.profile_pc);

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
            userEmail.setText(email);
        }else {
            Log.e("MainActivity",
                    "error setting use email to drawer.value: "  + userEmail);
        }
        // menu hamburger button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_black_36);
            getSupportActionBar().setTitle("Diet Stats");
        }
        //set drawer_menu Navigation listener
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
            }
        });

        // Set ViewModel.
        statsModel = ViewModelProviders.of(this).get(StatsViewModel.class);

        // set initial To Date
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Calendar c = Calendar.getInstance();
        toDate = c.getTime();
        String currDateString = formatter.format(toDate);
        toText.setText(currDateString);

        // set initial From Date
        c.add(Calendar.DAY_OF_MONTH, -6);  // go back 6 days
        fromDate = c.getTime(); // get set date
        String fromDateString = formatter.format(fromDate);
        fromText.setText(fromDateString);



        // From Date picker dialog
        setFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar initCalendar = Calendar.getInstance();
                initCalendar.setTime(fromDate);  // set initial date
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        DietStatsActivity.this,
                        initCalendar.get(Calendar.YEAR), // Initial year selection
                        initCalendar.get(Calendar.MONTH), // Initial month selection
                        initCalendar.get(Calendar.DAY_OF_MONTH) // Initial day selection
                );
                // set accent color
                dpd.setAccentColor(getResources().getColor(R.color.colorPrimaryLight));
                // show date picker dialog
                dpd.show(getSupportFragmentManager(), "Datepickerdialog_from");
            }
        });

        // To Date picker dialog
        setTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar initCalendar = Calendar.getInstance();
                initCalendar.setTime(fromDate); // set initial date
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        DietStatsActivity.this,
                        initCalendar.get(Calendar.YEAR), // Initial year selection
                        initCalendar.get(Calendar.MONTH), // Initial month selection
                        initCalendar.get(Calendar.DAY_OF_MONTH) // Initial day selection
                );
                // set accent color
                dpd.setAccentColor(getResources().getColor(R.color.colorPrimaryLight));
                // show date picker dialog
                dpd.show(getSupportFragmentManager(), "Datepickerdialog_to");
            }
        });




        // Network status check
        ConnectivityManager cm = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //Get the network status info
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        // When net work is available
        if (isConnected) {
            // Create observer for UI updates.
            final Observer<StatsData> StatsObserver = new Observer<StatsData>() {
                @Override
                public void onChanged(StatsData data) {
                    /*Log.d(TAG, "Observer onChanged triggered");*/
                    // If there is meals available
                    if (data != null) {
                        /*Log.d("StatsObserver", String.format("on taget days: %d",
                                data.getavgTarCal()));*/
                        String onTardays = data.getavgTarCal() + "%";
                        avgTarDays.setText(onTardays);
                        String onTarProt = data.getavgTarProt() + "%";
                        avgTarProt.setText(onTarProt);
                        String onTarCarbs = data.getAvgTarCarbs() + "%";
                        avgTarCarbs.setText(onTarCarbs);
                        String onTarFat = data.getAvgTarFat() + "%";
                        avgTarFat.setText(onTarFat);

                        if(data.getChartMacros().size() > 0){
                            // add data to graph
                            setChartData(data.getChartMacros());
                        }else{
                            Log.e("StatsObserver","Error chart data size: "
                                    + data.getChartMacros().size());
                        }

                    }
                }
            };
            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            statsModel.setUIData(fromDate, toDate).observe(this, StatsObserver);
        } else {
            /*Log.d(TAG, "No connection found");*/
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // Set a listener to the appropriate dialog if activity is resumed and dialog is still available
        // from date activity
        DatePickerDialog dpdFrom =  (DatePickerDialog) getSupportFragmentManager().findFragmentByTag("Datepickerdialog_from");
        if(dpdFrom != null) dpdFrom.setOnDateSetListener(this);
        // To date activity
        DatePickerDialog dpdTo =  (DatePickerDialog) getSupportFragmentManager().findFragmentByTag("Datepickerdialog_to");
        if(dpdTo != null) dpdTo.setOnDateSetListener(this);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String dateName =  dayOfMonth + "." + (monthOfYear+1) + "." + year;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try{
            date = formatter.parse(year + "-" + (monthOfYear+1)  + "-" + dayOfMonth);
        }catch(Exception e){
            Log.e("dietstats/onDateSet",  "Error parsing string date: " + e);
            date = Calendar.getInstance().getTime();
        }

        switch (view.getTag()){
            case "Datepickerdialog_from":
                // set from TextView date
                fromText.setText(dateName);
                toText.setText("...");
                fromDate = date;
                toDate = null;
                break;
            case "Datepickerdialog_to":
               if(fromDate.compareTo(date) < 0){
                   // set toText view date
                   toText.setText(dateName);
                   toDate = date;
                   // update UI
                   /*Log.d("onDateSet", "fromdate: " + fromDate + "toDate: " + toDate);*/
                   statsModel.setUIData(fromDate, toDate);
               }else{
                   // set toText view date
                   toText.setText("...");
                   Toast.makeText(DietStatsActivity.this, "Invalid range! please pick" +
                                   " a date after from date", Toast.LENGTH_LONG).show();
               }
                break;
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try{
            InputMethodManager inputMethodManager = (InputMethodManager)
                    activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), 0);
        }catch(Exception e){
            //Log.e(TAG, "Hide soft keyboard error: " + e);
        }
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
            /*case R.id.nav_ml_record:
                intent = new Intent(this, MealsRecordActivity.class);
                break;*/
            /*case R.id.nav_dt_stats:
                break;*/
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


    // ====================Update chart data====================//
    //*private void setChartData(int count, float range)*//
    // @param count the number of values on the x axis
    // @param range the range of the random values generated for testing
    // @param value the y value at point i
    // @param i is the x value which represents the days of the month starting from January 1st
    private void setChartData(ArrayList<DailyMacChart> macros) {

        chart.clear();
        chart.setOnChartValueSelectedListener(this);
        ValueFormatter xAxisFormatter = new DayAxisValueFormatter(macros);
        tfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
        //setting up chart
        chart.getDescription().setEnabled(false);
        // if more than 10 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);
        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);

        // optional config
        /*ValueFormatter custom = new MyValueFormatter("$");*/
        /*YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(tfLight);
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)*/

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawLabels(false);

        // optional config
        /*rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(tfLight);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)*/

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        XYMarkerView mv = new XYMarkerView(this, xAxisFormatter);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart

        // add a nice and smooth animation
        chart.animateY(1500);
        chart.animateX(1000);



        // ==================================Set chart data==================================//
        /*float start = 1f;

        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = (int) start; i < start + count; i++) {
            float val = (float) (Math.random() * (range + 1));

            values.add(new BarEntry(i, val));
            *//*values.add(new BarEntry(i, val, getResources().getDrawable(R.drawable.star)));*//*
        }*/


        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 0; i < macros.size(); i++){
            values.add(new BarEntry(i, (float) macros.get(i).getyVal()));
        }

        BarDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();

        } else {
            set1 = new BarDataSet(values, null);

            set1.setDrawIcons(false);


            // color styling
            /*set1.setColors(ColorTemplate.MATERIAL_COLORS);
            int startColor = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
            int endColor = ContextCompat.getColor(this, android.R.color.holo_blue_bright);
            set1.setGradientColor(startColor, endColor);*/
            int startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
            int startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light);
            int startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
            int startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light);
            int startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light);
            int endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
            int endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple);
            int endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark);
            int endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark);
            int endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
            List<GradientColor> gradientColors = new ArrayList<>();
            gradientColors.add(new GradientColor(startColor1, endColor1));
            gradientColors.add(new GradientColor(startColor2, endColor2));
            gradientColors.add(new GradientColor(startColor3, endColor3));
            gradientColors.add(new GradientColor(startColor4, endColor4));
            gradientColors.add(new GradientColor(startColor5, endColor5));
            set1.setGradientColors(gradientColors);


            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setValueTypeface(tfLight);
            data.setBarWidth(0.9f);

            /*Log.d("Chart", String.format("Bar Data data: %s", data.toString()));*/

            chart.setData(data);
        }
    }



    private final RectF onValueSelectedRectF = new RectF();

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;

        RectF bounds = onValueSelectedRectF;
        chart.getBarBounds((BarEntry) e, bounds);
        MPPointF position = chart.getPosition(e, YAxis.AxisDependency.LEFT);

        //Log.i("bounds", bounds.toString());
        //Log.i("position", position.toString());

        /*Log.i("x-index",
                "low: " + chart.getLowestVisibleX() + ", high: "
                        + chart.getHighestVisibleX());*/

        MPPointF.recycleInstance(position);
    }

    @Override
    public void onNothingSelected() { }

    private void chkAuth(){
        // Check if user is signed in (true). //mongoDB
        if (!stitchClient.getAuth().isLoggedIn()) {
            finish();
            //send user to login layout
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void showFeedbackDialog() {
        FeedbackDialog feedBackDialog = new FeedbackDialog();
        feedBackDialog.show(getSupportFragmentManager(), "feedBack");
    }

    @Override
    public void triggerSuccessToast() {
        Toast.makeText(DietStatsActivity.this, "FeedBack Sent. Thank you",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFailureToast() {
        Toast.makeText(DietStatsActivity.this, "Failed sending feedback!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboard() {
        hideSoftKeyboard(DietStatsActivity.this);
    }

    @Override
    public void detailsSuccessUpdate() {
        Toast.makeText(DietStatsActivity.this, "Details updated successfully",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void detailsFailureUpdate() {
        Toast.makeText(DietStatsActivity.this, "Failed updating details!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboardUpdate() {
        hideSoftKeyboard(DietStatsActivity.this);
    }
}
