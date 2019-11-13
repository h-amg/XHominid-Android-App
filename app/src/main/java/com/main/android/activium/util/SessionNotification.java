package com.main.android.activium.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.main.android.activium.ConsultationActivity;
import com.main.android.activium.R;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class SessionNotification {

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;
    private int mNotificationId;

    // {@param context} == Activity.getApplicationContext
    public SessionNotification(Context context, String notificationTitle,
                               String notificationText, int notificationId){

        // set notification ID
        mNotificationId = notificationId;

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the aDBEventIDctual notification channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel("Session update", "Session update", importance);

            // We can optionally add a description for the channel
            channel.setDescription("Session status update notification");
            //we can optionally set notification LED colour
            channel.setLightColor(Color.MAGENTA);
            // set sound
            /*Uri defailtsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);*/
            Uri activiumsound = Uri.parse("android.resource://com.main.android.activium/"
                    + R.raw.session_update_notification_sound);
            AudioAttributes audioAtr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            channel.setSound(activiumsound,audioAtr);
            // set vibration
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        //create an intent to open the event details activity
        Intent intent = new Intent(context, ConsultationActivity.class);

        //put together the PendingIntent
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 1, intent, FLAG_UPDATE_CURRENT);


        //build the notification
        notificationBuilder = new NotificationCompat.Builder(context, "Session update")
                .setSmallIcon(R.drawable.new_logo36x36_trans)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // set notification manager
        notificationManager = NotificationManagerCompat.from(context);

    }

    public void triggerNotificatio(){
        //trigger the notification
        // {@param mNotificationId = session id}
        //we give each notification the ID of the event it's describing,
        //to ensure they all show up and there are no duplicates
        notificationManager.notify(mNotificationId, notificationBuilder.build());
    }
}
