package com.main.android.activium.ViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.DataClasses.DailyMac;
import com.main.android.activium.util.DailyMacChart;
import com.main.android.activium.DataClasses.Diet;
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
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;

public class ChartViewModel extends ViewModel {

    private MutableLiveData<ArrayList<DailyMacChart>> statsLiveData;
    private Date mFromDate;
    private Date mToDate;
    private Document mdietDoc;

    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection dietsCollection;
    private RemoteMongoCollection dailyMacCollection;



    public LiveData<ArrayList<DailyMacChart>> getChartData(Date fromDate, Date toDate) {
        if (statsLiveData == null) {
            statsLiveData = new MutableLiveData<ArrayList<DailyMacChart>>();
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
        dietsCollection = mongoDBClient.getDatabase("database_namep")
                .getCollection("collection_name");
        dailyMacCollection = mongoDBClient.getDatabase("database_namep")
                .getCollection("collection_name");


        // fetch user diet data
        final Task<Document> findTask = dietsCollection.find().limit(1).first();
        findTask.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull Task <Document> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null) {
                        Log.e("ChartViewModel", "Could not find any matching diet documents");
                    } else {
                        // fetch user's today's macros
                        mdietDoc = task.getResult();
                        getTodayMac();
                    }
                } else {
                    Log.e("ChartViewModel", "failed to find diet document with: ", task.getException());

                }
            }
        });
    }

    // fetch user today's macros record
    private void getTodayMac(){
        ArrayList<DailyMacChart> chartMacros = new ArrayList<DailyMacChart>();

        // Fetch user meals record for the day
        final RemoteFindIterable find = dailyMacCollection.find(and(
                gte("timestamp", mFromDate),
                lt("timestamp", mToDate)));
        Task <List<Document>> itemsTask = find.into(new ArrayList<Document>());

        itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null){
                        Log.e("ChartViewModel", "No documents found");
                        statsLiveData.setValue(null);
                    }else{
                        /*Log.d("ChartViewModel", String.format("successfully found %d documents",
                                task.getResult().size()));*/
                        for(Document doc:task.getResult()){
                            chartMacros.add(new DailyMacChart(new DailyMac(doc), new Diet(mdietDoc)));
                            // set stats liveData
                            statsLiveData.setValue(chartMacros);
                        }
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
