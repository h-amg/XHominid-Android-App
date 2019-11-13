package com.main.android.activium.DataClasses;

import android.util.Log;

import org.bson.Document;

import java.util.Date;

public class Diet {

    private Double mTar_prto;
    private Double mTar_carbs;
    private Double mTar_fat;
    private Double mTar_cal;


    public Diet(Document doc ){

        try {
            mTar_prto = (double) doc.get("target_protein");
            mTar_carbs = (double) doc.get("target_carbs");
            mTar_fat = (double) doc.get("target_fat");
            mTar_cal = (double) doc.get("target_calories");
        }catch (Exception e){
            Log.e("Diet", "Error retrieving diet targets from diet document");
        }
    }


    public Double getProt(){
        return mTar_prto;
    }

    public Double getCarbs(){
        return mTar_carbs;
    }

    public Double getFat(){
        return mTar_fat;
    }

    public Double getCal(){
        return mTar_cal;
    }



}
