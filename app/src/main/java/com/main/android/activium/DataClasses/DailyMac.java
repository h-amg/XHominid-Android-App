package com.main.android.activium.DataClasses;

import android.util.Log;

import org.bson.Document;

import java.util.Date;

public class DailyMac {

    private Double mCons_prto;
    private Document mMacrosDoc;
    private Double mCons_carbs;
    private Double mCons_fat;
    private Double mCons_cal;
    private Date   mTimeStamp;

    public DailyMac(Document doc ){

        try{
            mTimeStamp = (Date) doc.get("timestamp");
            mMacrosDoc = (Document) doc.get("consumed_macros");
            mCons_prto = (double) mMacrosDoc.get("protein");
            mCons_carbs = (double) mMacrosDoc.get("carbs");
            mCons_fat = (double) mMacrosDoc.get("fat");
            mCons_cal = (double) doc.get("consumed_calories");
        }catch (Exception e){
            Log.e("DailyMac", "Error extracting data from docuemnt retrieved: " + e);
            mCons_prto = 0.0;
            mMacrosDoc = null;
            mCons_carbs = 0.0;
            mCons_fat = 0.0;
            mCons_cal = 0.0;
            mTimeStamp = null;

        }
    }

    public Date getTime(){
        return mTimeStamp;
    }

    public Double getProt(){
        return mCons_prto;
    }

    public Double getCarbs(){
        return mCons_carbs;
    }

    public Double getFat(){
        return mCons_fat;
    }

    public Double getCal(){
        return mCons_cal;
    }



}
