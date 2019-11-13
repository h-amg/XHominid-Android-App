package com.main.android.activium.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.RemoteMessage;
import com.main.android.activium.MainActivity;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private final String TAG = "FirebaseMessaging";

    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection usersCollection;
    final StitchUser user = stitchClient.getAuth().getUser();
    private String uId;
    final Date currDay = Calendar.getInstance().getTime();

    private String mToken;

    @Override
    public void onNewToken(String token) {
        //Log.d(TAG, "Refreshed token: " + token);
        // send token to DB
        sendToken(token);
        // subscribe user to topic all
        subscribeToAll();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //find out who the message is coming from
        /*Log.d(TAG, "From: " + remoteMessage.getFrom());*/

        String title;
        String text;
        int delay;
        int id;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            /*Log.d(TAG, "Message data payload: " + remoteMessage.getData());*/
            if (remoteMessage.getData().get("isSessNotif").contentEquals("true")) {

                if (remoteMessage.getData().get("id") != null
                        || remoteMessage.getData().get("title") != null
                        ||remoteMessage.getData().get("text") != null
                        ||remoteMessage.getData().get("delay") != null){

                    title = remoteMessage.getData().get("title");
                    text = remoteMessage.getData().get("text");
                    delay = Integer.valueOf(remoteMessage.getData().get("delay")); // dekay in millisecond
                    id = Integer.valueOf(remoteMessage.getData().get("id"));
                    Log.d(TAG, "Session notification params found, title: " + title +
                            " text: " + text + " delay: " + delay + " id: " + id);
                    scheduleSessNotification(id, title, text, delay);
                }else{
                    Log.e(TAG, "Session notification params not found");
                }
            }else if (remoteMessage.getData().get("isFeedbackNotig").contentEquals("true")) {
                if (remoteMessage.getData().get("id") != null
                        || remoteMessage.getData().get("title") != null
                        ||remoteMessage.getData().get("text") != null){

                    title = remoteMessage.getData().get("title");
                    text = remoteMessage.getData().get("text");
                    id = Integer.valueOf(remoteMessage.getData().get("id"));
                    /*Log.d(TAG, "feedBack notification params found, title: " + title +
                            " text: " + text + " id: " + id);*/
                    scheduleFeedbackNotif(id, title, text);
                }else{
                    Log.e(TAG, "Feedback notification params not found");
                }
            }
        }
        /*// Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }*/
    }

    private void scheduleSessNotification(int id, String title, String text, int delay){
        //set a tag to be able to cancel all work of this type if needed
        String workTag = String.valueOf(id);

        // store paramters to pass to worker
        Data inputData = new Data.Builder()
                .putInt("id", id)
                .putString("title", title)
                .putString("text", text)
                .build();

        OneTimeWorkRequest sessNotifWork = new OneTimeWorkRequest.Builder(NotifySessionWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(workTag)
                .build();

        WorkManager.getInstance(this
        ).beginUniqueWork(workTag, ExistingWorkPolicy.REPLACE, sessNotifWork).enqueue();
    }

    private void scheduleFeedbackNotif(int id, String title, String text){
        //set a tag to be able to cancel all work of this type if needed
        String workTag = String.valueOf(id);

        // store paramters to pass to worker
        Data inputData = new Data.Builder()
                .putInt("id", id)
                .putString("title", title)
                .putString("text", text)
                .build();

        OneTimeWorkRequest feedBackNotifWork = new OneTimeWorkRequest.Builder(NotifyFeedbackWorker.class)
                .setInputData(inputData)
                .addTag(workTag)
                .build();

        WorkManager.getInstance(this
        ).beginUniqueWork(workTag, ExistingWorkPolicy.REPLACE, feedBackNotifWork).enqueue();
    }



    private void sendToken(String token){
        if(user != null) {
            uId = user.getId();
        }else{
            Log.e(TAG, "error updating user toke: user not logged in");
            return;
        }
        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        usersCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        Document filterDoc = new Document().append("uId",uId);
        Document updateDoc = new Document().append("$set",
                new Document()
                        .append("messagingToken", token)
                        .append("tokenDate", currDay)
        );

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);

        final Task<RemoteUpdateResult> updateTask =
                usersCollection.updateOne(filterDoc, updateDoc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener<RemoteUpdateResult>() {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    /*Log.d("FirebaseMessaging", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));*/
                } else {
                    Log.e("startAnalysis", "failed to update document with: ",
                            task.getException());
                }
            }
        });
    }

    private void subscribeToAll(){
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("weather")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e("FirebaseMessaging", "User subscription failed!");
                        }else{
                            Log.d("FirebaseMessaging", "User subscribed to all successfully");
                        }
                    }
                });
    }
}
