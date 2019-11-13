package com.main.android.activium.DataClasses;


import android.util.Log;

import org.bson.Document;

import java.util.Date;

public class Sessions {

    // Initiate variables
    private String sessionId;
    private String consultantImgUrl;
    private String consulteeImgUrl;
    private String consultantName;
    private String consulteeName;
    private String roomName;
    private Date dateTiem;
    private Boolean approved;
    private Boolean completed;
    private Boolean missed;
    private Boolean cancelled;
    private Boolean isNutritionist;
    private Boolean scheduled;
    private Boolean beingScheduled = false;

    //custom constructor for testing
    public Sessions(Document sessDoc){

        try {
            sessionId = sessDoc.getObjectId("_id").toString();
            //Log.d("Sessions", "sessionId: " + sessionId);
            consultantImgUrl = sessDoc.getString("consultantImgUrl");
            /*Log.d("Sessions", "consultantImgUrl: " + consultantImgUrl);*/
            consulteeImgUrl = sessDoc.getString("consulteeImgUrl");
            /*Log.d("Sessions", "consulteeImgUrl: " + consulteeImgUrl);*/
            consultantName = sessDoc.getString("consultantName");
            /*Log.d("Sessions", "consultantName: " + consultantName);*/
            consulteeName = sessDoc.getString("consulteeName");
            /*Log.d("Sessions", "consulteeName: " + consulteeName);*/
            roomName = sessDoc.getString("roomName");
            /*Log.d("Sessions", "roomName: " + roomName);*/
            dateTiem = sessDoc.getDate("dateTime");
            /*Log.d("Sessions", "dateTiem: " + dateTiem);*/
            isNutritionist = sessDoc.getBoolean("isNutritionist", false);
            /*Log.d("Sessions", "isNutritionist: " + isNutritionist);*/
            approved = sessDoc.getBoolean("approved", false);
            /*Log.d("Sessions", "approved: " + approved);*/
            completed = sessDoc.getBoolean("completed", false);
            /*Log.d("Sessions", "completed: " + completed);*/
            missed = sessDoc.getBoolean("missed", false);
            /*Log.d("Sessions", "missed: " + missed);*/
            cancelled = sessDoc.getBoolean("cancelled", false);
            /*Log.d("Sessions", "cancelled: " + cancelled);*/
            scheduled = sessDoc.getBoolean("scheduled", false);
            /*Log.d("Sessions","scheduled: " + scheduled);*/
        }catch (Exception e){
            Log.e("Sessions", "Error retrieving data from Sessions document");
        }
    }

    public String getSessionId(){
        return sessionId;
    }

    public String getConsultantImgUrl(){
        return consultantImgUrl;
    }

    public String getConsulteeImgUrl(){
        return consulteeImgUrl;
    }

    public String getConsultantName(){
        return consultantName;
    }

    public String getConsulteeName(){
        return consulteeName;
    }

    public String getRoomName(){
        return roomName;
    }

    public Date getDateTiem(){
        return dateTiem;
    }

    public Boolean getApproved(){
        return approved;
    }

    public Boolean getCompleted(){
        return completed;
    }

    public Boolean getMissed(){
        return missed;
    }

    public Boolean getCancelled(){
        return cancelled;
    }

    public Boolean getScheduled(){
        return scheduled;
    }

    public Boolean getIsNutritionist(){
        return isNutritionist;
    }

    public Boolean getBeingScheduled(){
        return beingScheduled;
    }

    public void setBeingScheduled (boolean state){
        beingScheduled = state;
    }
}
