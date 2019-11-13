package com.main.android.activium.util;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.main.android.activium.dialogs.RateEnergyDialog;

public class NotifyFeedbackWorker extends Worker {

    private Context mContext;

    public NotifyFeedbackWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Method to trigger an instant notification
        triggerNotification();

        return Result.success();
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }



    private void triggerNotification (){
        // Notification params
        String title =  getInputData().getString("title");
        String text =  getInputData().getString("text");
        int Id =  getInputData().getInt("id", 0);

        //create session notification
        FeedBackNotification notification = new FeedBackNotification(mContext, title, text, Id);
        // Trigger notification
        notification.triggerNotificatio();
    }
}
