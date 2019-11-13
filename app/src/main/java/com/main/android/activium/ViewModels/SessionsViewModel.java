package com.main.android.activium.ViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.DataClasses.Sessions;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SessionsViewModel extends ViewModel {

    private MutableLiveData<ArrayList<Sessions>> upcomingSessionsLiveData;
    private MutableLiveData<ArrayList<Sessions>> TodaysSessionsLiveData;

    // MongoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoCollection sessionCollection;
    private RemoteMongoClient mongoDBClient;
    final StitchUser user = stitchClient.getAuth().getUser();

    //user
    private String uId;

    public LiveData<ArrayList<Sessions>> getUpcomingSessions() {
        if (upcomingSessionsLiveData == null) {
            upcomingSessionsLiveData = new MutableLiveData<ArrayList<Sessions>>();
        }
        loadSessions();
        return upcomingSessionsLiveData;
    }

    public LiveData<ArrayList<Sessions>> getTodaysSessions() {
        if (TodaysSessionsLiveData == null) {
            TodaysSessionsLiveData = new MutableLiveData<ArrayList<Sessions>>();
        }
        loadSessions();
        return TodaysSessionsLiveData;
    }

    private void loadSessions() {

        // get current DateTime
        Calendar currDateTimeCalender = Calendar.getInstance();
        Date currDateTime = currDateTimeCalender.getTime();
        // current Date
        Calendar currDateCalendar = Calendar.getInstance();
        currDateCalendar.setTime( currDateTime );
        currDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currDateCalendar.set(Calendar.MINUTE, 0);
        currDateCalendar.set(Calendar.SECOND, 0);
        currDateCalendar.set(Calendar.MILLISECOND, 0);
        Date  currDate = currDateCalendar.getTime();
        //Log.d("SessionsViewModel", "currDate: " + currDate);

        if(user != null){
            uId = user.getId();
            /*Log.d("SessionsViewModel", "user Id: " + uId);*/
        }

        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        sessionCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        // Initiate a  upcomingSessionsList instance
        ArrayList<Sessions> upcomingSessionsList = new ArrayList<>();

        // Initiate a  todaysSessionsList instance
        ArrayList<Sessions> todaysSessionsList = new ArrayList<>();

        // filter document
        Document filterDoc = new Document()
                .append("consulteeId", uId)
                .append("completed", false)
                .append("missed", false)
                .append("cancelled", false);

        // Fetch user sessions record
        final RemoteFindIterable find = sessionCollection.find(filterDoc);
        Task <List<Document>> itemsTask = find.into(new ArrayList<Document>());
        itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null){
                        //Log.d("SessionsViewModel", "No Documents found!");
                        upcomingSessionsLiveData.setValue(null);
                        TodaysSessionsLiveData.setValue(null);
                    }else {
                        List<Document> sessionsDocs = task.getResult();
                        /*Log.d("SessionsViewModel", String.format("successfully found %d documents",
                                sessionsDocs.size()));*/
                        if (sessionsDocs.size() != 0){
                            for (Document session : sessionsDocs) {

                                Boolean approved = session.getBoolean("approved",
                                        false);
                                /*Log.d("SessionsViewModel","approved: " + approved);*/
                                Boolean completed = session.getBoolean("completed",
                                        false);
                                /*Log.d("SessionsViewModel","completed: " + completed);*/
                                Boolean missed = session.getBoolean("missed",
                                        false);
                                /*Log.d("SessionsViewModel","missed: " + missed);*/
                                Boolean cancelled = session.getBoolean("cancelled",
                                        false);
                                /*Log.d("SessionsViewModel","cancelled: " + cancelled);*/
                                Boolean scheduled = session.getBoolean("scheduled",
                                        false);
                                /*Log.d("SessionsViewModel","scheduled: " + scheduled);*/
                                Date sessionDateTime = session.getDate("dateTime");
                                /*Log.d("SessionsViewModel", "dateTiem: " + dateTiem);*/

                                // month after current sessions month
                               /* Calendar calenderMonth_plus1 = Calendar.getInstance();
                                calenderMonth_plus1.setTime(sessionDateTime);
                                calenderMonth_plus1.add(Calendar.MONTH, 1);
                                Date nextMonth = calenderMonth_plus1.getTime();*/

                                //  15 minutes after current session
                                Date sessionDate_plus_15min = null;
                                if (sessionDateTime != null) {
                                    Calendar calenderPls = Calendar.getInstance();
                                    calenderPls.setTime(sessionDateTime);
                                    calenderPls.add(Calendar.MINUTE, +15);
                                    sessionDate_plus_15min = calenderPls.getTime();
                                }

                                //  15 minutes before current session
                                /*Date sessionDate_Min_15min = null;
                                if (sessionDateTime != null) {
                                    Calendar calenderMin15 = Calendar.getInstance();
                                    calenderMin15.setTime(sessionDateTime);
                                    calenderMin15.add(Calendar.MINUTE, -15);
                                    sessionDate_Min_15min = calenderMin15.getTime();
                                }*/

                                // current session Date
                                Date currSessDate = null;
                                if (sessionDateTime != null) {
                                    Calendar currSessDateCalendar = Calendar.getInstance();
                                    currSessDateCalendar.setTime(sessionDateTime);
                                    currSessDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                                    currSessDateCalendar.set(Calendar.MINUTE, 0);
                                    currSessDateCalendar.set(Calendar.SECOND, 0);
                                    currSessDateCalendar.set(Calendar.MILLISECOND, 0);
                                    currSessDate = currSessDateCalendar.getTime();
                                    //Log.d("SessionsViewModel", "currSessDate: " + currSessDate);
                                }

                                // if the session is not cancelled or missed or completed
                                if (!missed && !cancelled && !completed) {
                                    // if the session is approved and scheduled and 15 minutes after session is in the future
                                    // and the session is happening today
                                    /*Log.d("SessionsViewModel", "condition 1: currDateTime = "
                                            + currDateTime + ", sessionDate_plus_15min = "
                                            + sessionDate_plus_15min + ", currDate = "
                                            + currDate + ", currSessDate = " + currSessDate
                                            + ", approved = " + approved
                                            + ", scheduled = " + scheduled);*/
                                    if (sessionDate_plus_15min != null && currSessDate != null &&
                                            (currDateTime.compareTo(sessionDate_plus_15min) < 0)
                                            && (currDate.compareTo(currSessDate) == 0)
                                            && approved && scheduled) {
                                        /*Log.d("SessionsViewModel",
                                                "condition 1 passed, sessionDateTime: " + sessionDateTime);*/
                                        todaysSessionsList.add(new Sessions(session));
                                    }
                                    // if 15 minutes after session is in the past
                                    else if (sessionDate_plus_15min != null && currSessDate != null
                                            && currDateTime.compareTo(sessionDate_plus_15min) > 0) {
                                        // mark session as missed
                                    }
                                    // if 15 minutes before the session is still in the future
                                    else {
                                        upcomingSessionsList.add(new Sessions(session));
                                    }
                                    // set sessions liveDatas
                                    upcomingSessionsLiveData.setValue(upcomingSessionsList);
                                    TodaysSessionsLiveData.setValue(todaysSessionsList);
                                } else {
                                    Log.e("SessionsViewModel", "failed to find sessions documents with: ",
                                            task.getException());
                                    upcomingSessionsLiveData.setValue(null);
                                    TodaysSessionsLiveData.setValue(null);
                                }
                            }
                        }else{
                            /*Log.d("SessionsViewModel", "No sessions documents found",
                                    task.getException());*/
                            upcomingSessionsLiveData.setValue(null);
                            TodaysSessionsLiveData.setValue(null);
                        }
                    }
                }
            }
        });
    }
}

