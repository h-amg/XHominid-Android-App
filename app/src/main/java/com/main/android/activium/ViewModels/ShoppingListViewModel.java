package com.main.android.activium.ViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.DataClasses.LoggedMeal;
import com.main.android.activium.DataClasses.ShoppingItem;
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
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;

public class ShoppingListViewModel extends ViewModel {

    private MutableLiveData<ArrayList<ShoppingItem>> ShoppingItemsLiveData;

    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoCollection shoppingItemsCollection;
    private RemoteMongoClient mongoDBClient;


    public LiveData<ArrayList<ShoppingItem>> getShoppingItems(boolean getNextWeek) {
        if (ShoppingItemsLiveData == null) {
            ShoppingItemsLiveData = new MutableLiveData<ArrayList<ShoppingItem>>();
        }
        fetchShoppingList(getNextWeek);
        return ShoppingItemsLiveData;
    }


    private void fetchShoppingList(boolean getNextWeek) {
        // Do an asynchronous operation to fetch users.

        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        shoppingItemsCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        // Initiate a  mealsList instance
        ArrayList<ShoppingItem> shoppingItems = new ArrayList<>();

        Date weekStart;
        Date weekEnd;
        WeekBoundaries currWeek = new WeekBoundaries();
        if (getNextWeek){
            // set filter params to the start and end of next week
            weekStart = currWeek.getmNextWeekStart();
            weekEnd = currWeek.getmNextWeekEnd();
        }else{
            // set filter params to the start and end of this week
            weekStart = currWeek.getmWeekStart();
            weekEnd = currWeek.getmWeekEnd();
        }

        // Fetch user meals record for the day
        final RemoteFindIterable find = shoppingItemsCollection.find(and(
                gte("validOn", weekStart),
                lt("validOn", weekEnd)));
        Task <List<Document>> itemsTask = find.into(new ArrayList<Document>());
        itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null){
                        Log.e("viewModel", "No Documents found!");
                        ShoppingItemsLiveData.setValue(null);
                    }else{
                        List<Document> shoppingItemDocs = task.getResult();
                        /*Log.d("shoppingListViewModel", String.format("successfully found %d documents",
                                shoppingItemDocs.size()));*/
                        for (Document shoppingItemDoc: shoppingItemDocs) {
                            shoppingItems.add(new ShoppingItem(shoppingItemDoc));
                        }
                        // set meals liveData
                        ShoppingItemsLiveData.setValue(shoppingItems);
                    }
                } else {
                    Log.e("shoppingListViewModel", "failed to find documents with: ", task.getException());
                    ShoppingItemsLiveData.setValue(null);
                }
            }
        });
    }
}
