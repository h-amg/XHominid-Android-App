package com.main.android.activium.ViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.util.DayBoundaries;
import com.main.android.activium.DataClasses.LoggedMeal;
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

public class MealViewModel extends ViewModel {

    private MutableLiveData<ArrayList<LoggedMeal>> mealsLiveData;

    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoCollection mealsCollection;
    private RemoteMongoClient mongoDBClient;


    public LiveData<ArrayList<LoggedMeal>> getMeals(Date day) {
        if (mealsLiveData == null) {
            mealsLiveData = new MutableLiveData<ArrayList<LoggedMeal>>();
        }
        loadMeals(day);
        return mealsLiveData;
    }



    private void loadMeals(Date day) {
        // Do an asynchronous operation to fetch users.

        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        mealsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");


        // Initiate a  mealsList instance
        ArrayList<LoggedMeal> mealsList = new ArrayList<>();

        // Get day start and day end Date object for query filtering
        Date dayStart = new DayBoundaries(day).getDayStart();
        Date dayEnd = new DayBoundaries(day).getDayEnd();

        // Fetch user meals record for the day
        final RemoteFindIterable find = mealsCollection.find(and(
                gte("timestamp", dayStart),
                lt("timestamp", dayEnd)));
        Task <List<Document>> itemsTask = find.into(new ArrayList<Document>());
        itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null){
                        Log.e("viewModel", "No Documents found!");
                        mealsLiveData.setValue(null);
                    }else{
                        List<Document> meals = task.getResult();
                        /*Log.d("viewModel", String.format("successfully found %d documents",
                                meals.size()));*/
                        for (Document meal: meals) {
                            mealsList.add(new LoggedMeal(meal));
                            /*Log.d("MealViewModel", String.format("No. of meals added: %d",
                                    mealsList.size()));*/
                        }
                        // set meals liveData
                        mealsLiveData.setValue(mealsList);
                    }
                } else {
                    Log.e("viewModel", "failed to find documents with: ", task.getException());
                    mealsLiveData.setValue(null);
                }
            }
        });
    }
}
