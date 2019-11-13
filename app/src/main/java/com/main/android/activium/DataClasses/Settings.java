package com.main.android.activium.DataClasses;

import android.util.Log;

import org.bson.Document;

public class Settings {

    private boolean mac_log_rmnd_state;
    private boolean diet_status_rmnd_state;
    private boolean audio_state;
    private int mac_log_rmnd_freeq;
    private int diet_status_rmnd_freeq;



    public Settings(Document settings ){

        try {
            Document mac_log_rmnd = (Document) settings.get("macro_logging_rmnd");
            Document diet_status_rmnd = (Document) settings.get("diet_status_rmnd");
            mac_log_rmnd_state = (boolean) mac_log_rmnd.get("state");
            mac_log_rmnd_freeq = (int) mac_log_rmnd.get("frequency");
            diet_status_rmnd_state = (boolean) diet_status_rmnd.get("state");
            diet_status_rmnd_freeq = (int) diet_status_rmnd.get("frequency");
            audio_state = (boolean) settings.get("audio");
        }catch (Exception e){
            Log.e("Settings", "Error retrieving data from Settings document");
        }
    }


    public boolean getMacLogrmndState(){
        return mac_log_rmnd_state;
    }

    public int getMacLogrmndFrq(){
        return mac_log_rmnd_freeq;
    }

    public boolean getDietStatusRmndState(){
        return diet_status_rmnd_state;
    }

    public int getDietStatusRmndFrq(){
        return diet_status_rmnd_freeq;
    }

    public boolean getAudioState(){
        return audio_state;
    }



}
