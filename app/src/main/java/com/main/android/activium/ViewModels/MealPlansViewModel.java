package com.main.android.activium.ViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.DataClasses.MainUiData;
import com.main.android.activium.DataClasses.RecomndRecipe;
import com.main.android.activium.util.DayBoundaries;
import com.main.android.activium.util.WeekBoundaries;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;

public class MealPlansViewModel extends ViewModel {

    private final String TAG = "MealPlansViewModel";

    private MutableLiveData< ArrayList<RecomndRecipe>> mealPlansData;

    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection recomndsCollection;
    private Date mCurrDay;
    private String mUId;
    private Date dayStart;
    private Date dayEnd;


    public LiveData<ArrayList<RecomndRecipe>> getUiData(Date day, String uId) {
        mUId = uId;
        mCurrDay = day;
        dayStart = new DayBoundaries(mCurrDay).getDayStart();
        dayEnd = new DayBoundaries(mCurrDay).getDayEnd();

        if (mealPlansData == null) {
            mealPlansData = new MutableLiveData< ArrayList<RecomndRecipe>>();
        }
        startLoadingData();
        return mealPlansData;
    }


    private void startLoadingData() {

        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        recomndsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        // Get week start and weekend
        Date weekStart;
        Date weekEnd;
        WeekBoundaries currWeek = new WeekBoundaries();
        // set filter params to the start and end of this week
        weekStart = currWeek.getmWeekStart();
        weekEnd = currWeek.getmWeekEnd();
        //Log.d(TAG, "weekStart: " + weekStart + " weekEnd: " + weekEnd);

        // Fetch user meals record for the day
        final RemoteFindIterable find = recomndsCollection.find(and(
                gte("validOn", dayStart),
                lt("validOn", dayEnd)));

        Task <List<Document>> itemsTask = find.into(new ArrayList<Document>());
        itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0){
                        // enable weekly cravings if the current day is a week after last weekly cravings set
                        /*if (mCurrDay.compareTo(weekEnd) >= 0 || mCravSetDate == null){
                         *//*enableWeeklyCraving();*//*
                        }*/
                        mealPlansData.setValue(null);
                        Log.e(TAG, "No recomnds Documents found!");
                    }else{
                        /*Log.d("viewModel/remond", "Documents found successfully! document: " +
                                task.getResult().toString());*/
                        // Initiate a  recomndsList instance
                        ArrayList<RecomndRecipe> recomndsList = new ArrayList<RecomndRecipe>();
                        // List of documents found
                        List<Document> foundlist = task.getResult();
                        /* Log.d("viewModel/remond", "foundlist is empty: " + foundlist.isEmpty());*/
                        for (Document recomnd: foundlist) {
                            recomndsList.add(new RecomndRecipe(recomnd));
                        }
                        mealPlansData.setValue(recomndsList);
                    }
                } else {
                    mealPlansData.setValue(null);
                    Log.e(TAG, "failed to find recomnds documents with: ", task.getException());
                }
            }
        });

    }

}
