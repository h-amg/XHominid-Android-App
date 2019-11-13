package com.main.android.activium.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotifySessionWorker extends Worker {

    private Context mContext;

    public NotifySessionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
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
        SessionNotification notification = new SessionNotification(mContext, title, text, Id);
        // Trigger notification
        notification.triggerNotificatio();
    }
}
