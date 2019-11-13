package com.main.android.activium.ViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.DataClasses.StatsData;
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

public class StatsViewModel extends ViewModel {

    private MutableLiveData<StatsData> statsLiveData;
    private Date mFromDate;
    private Date mToDate;

    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection dietsCollection;
    private RemoteMongoCollection dailyMacCollection;



    public LiveData<StatsData> setUIData(Date fromDate, Date toDate) {
        if (statsLiveData == null) {
            statsLiveData = new MutableLiveData<StatsData>();
        }
        mFromDate = fromDate;
        mToDate   = toDate;
        loadMeals();
        return statsLiveData;
    }



    private void loadMeals() {
        // Do an asynchronous operation to fetch users.

        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        dietsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");
        dailyMacCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");


        // Initiate a  mealsList instance
        // TODO: CREATE A CLASS TO UPDATE STATS UI + AC LASS FOR TABLE DATA UPDATE

        // fetch user diet data
        final Task<Document> findTask = dietsCollection.find().limit(1).first();
        findTask.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull Task <Document> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null) {
                        Log.e("StatsViewModel", "Could not find any matching diet documents");
                    } else {
                        /*Log.d("app", String.format("successfully found diet document: %s",
                                task.getResult().toString()));*/
                        // fetch user's today's macros
                        getTodayMac(task.getResult());
                    }
                } else {
                    Log.e("StatsViewModel", "failed to find diet document with: ", task.getException());

                }
            }
        });
    }

    // fetch user today's macros record
    private void getTodayMac(Document dietDoc){
        // Fetch user meals record for the day
        final RemoteFindIterable find = dailyMacCollection.find();
        Task <List<Document>> itemsTask = find.into(new ArrayList<Document>());
        itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null){
                        Log.e("viewModel", "No documents found");
                        statsLiveData.setValue(null);
                    }else{
                        /*Log.d("viewModel", String.format("successfully found %d documents",
                                task.getResult().size()));*/
                        StatsData data = new StatsData(dietDoc, task.getResult(), mFromDate, mToDate);
                        // set stats liveData
                        statsLiveData.setValue(data);
                    }
                } else {
                    Log.e("viewModel", "failed to find documents with: ",
                            task.getException());
                    statsLiveData.setValue(null);
                }
            }
        });
    }
}
