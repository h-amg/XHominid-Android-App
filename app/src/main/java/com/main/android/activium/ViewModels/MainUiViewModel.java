package com.main.android.activium.ViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.util.DayBoundaries;
import com.main.android.activium.DataClasses.MainUiData;
import com.main.android.activium.DataClasses.RecomndRecipe;
import com.main.android.activium.util.WeekBoundaries;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;

public class MainUiViewModel extends ViewModel {

    private final String TAG = "MainUiViewModel";

    private MutableLiveData<MainUiData> mainUiLiveData;

    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection dietsCollection;
    private RemoteMongoCollection dailyMacCollection;
    private RemoteMongoCollection recomndsCollection;
    private Date mCurrDay;
    private String mUId;
    private Date dayStart;
    private Date dayEnd;


    public LiveData<MainUiData> getUiData(Date day, String uId) {
        mUId = uId;
        mCurrDay = day;
        dayStart = new DayBoundaries(mCurrDay).getDayStart();
        dayEnd = new DayBoundaries(mCurrDay).getDayEnd();

        if (mainUiLiveData == null) {
            mainUiLiveData = new MutableLiveData<MainUiData>();
        }
        startLoadingData();
        return mainUiLiveData;
    }


    private void startLoadingData() {

        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        dietsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        // fetch user diet data
        final Task <Document> findTask = dietsCollection.find().limit(1).first();
        findTask.addOnCompleteListener(new OnCompleteListener <Document> () {
            @Override
            public void onComplete(@NonNull Task <Document> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null) {
                        getTodayMac(null);
                        Log.e("MainUI", "Could not find any matching diet documents");
                    } else {
                        /*Log.d("app", String.format("successfully found diet document: %s",
                                task.getResult().toString()));*/
                        // fetch user's today's macros

                        // get creation date of last weekly cravings (mealPlans)
                        // "cravingsetdate" value set after weekly craving/meal plan is created
                        getTodayMac(task.getResult());
                    }
                } else {
                    getTodayMac(null);
                    Log.e("MainUI", "failed to find diet document with: ", task.getException());
                }
            }
        });
    }

    // fetch user today's macros record
    private void getTodayMac(Document dietDoc){

        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        dailyMacCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        final Task <Document> findTask = dailyMacCollection.find(and(
                gte("timestamp", dayStart),
                lt("timestamp", dayEnd)))
                .limit(1).first();
        findTask.addOnCompleteListener(new OnCompleteListener <Document> () {
            @Override
            public void onComplete(@NonNull Task <Document> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null) {
                        getRecomnds(dietDoc, null);
                        Log.e("MainUI", "Could not find any matching dailyMacs documents");
                    } else {
                        /*Log.d("app", String.format("successfully found dailyMacs document: %s",
                                task.getResult().toString()));*/
                        getRecomnds(dietDoc, task.getResult());
                    }
                } else {
                    getRecomnds(dietDoc, null);
                    Log.e("MainUI", "failed to find dailyMacs document with: ", task.getException());
                }
            }
        });
    }

    private void getRecomnds(Document dietDoc, Document dailyMacDoc){

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
                lt("validOn", dayEnd),
                eq("eaten", false)));

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
                        mainUiLiveData.setValue(new MainUiData(dietDoc, dailyMacDoc, null));
                        Log.e("MainUI", "No recomnds Documents found!");
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
                        mainUiLiveData.setValue(new MainUiData(dietDoc, dailyMacDoc, recomndsList));
                    }
                } else {
                    mainUiLiveData.setValue(new MainUiData(dietDoc, dailyMacDoc, null));
                    Log.e("MainUI", "failed to find recomnds documents with: ", task.getException());
                }
            }
        });
    }

    /*private void enableWeeklyCraving(){
        Document filterDoc = new Document().append("uId",mUId);
        Document updateDoc = new Document().append("$set",
                new Document()
                        .append("sessCompletConfrm", false)
                        .append("mealPlanGen", false)
        ); // set weekly  cravings status to not generate nor set by the user

        final Task<RemoteUpdateResult> updateTask =
                dietsCollection.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    *//*Log.d("MainUI", String.format("successfully matched %d and modified %d " +
                                    "diet documents", numMatched, numModified));*//*

                } else {
                    Log.e("MainUI", "failed to update document with: ",
                            task.getException());
                }
            }
        });
    }*/
}
