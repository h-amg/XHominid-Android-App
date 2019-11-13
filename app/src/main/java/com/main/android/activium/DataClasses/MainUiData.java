package com.main.android.activium.DataClasses;

import android.util.Log;

import com.main.android.activium.DataClasses.RecomndRecipe;

import org.bson.Document;

import java.util.ArrayList;

public class MainUiData {

    private Document mDietDoc;
    private Document mDailyMacDoc;
    private ArrayList<RecomndRecipe> mRecomndsList;


    public MainUiData(Document dietDoc, Document dailyMacDoc, ArrayList<RecomndRecipe> recomnds){
        try {
            mDietDoc = dietDoc;
            mDailyMacDoc = dailyMacDoc;
            mRecomndsList = recomnds;
        }catch (Exception e){
            Log.e("MainUiData", "Error retrieving data from MainUiData document");
        }
    }


    public Document getdietDoc(){
        return mDietDoc;
    }

    public Document getDailyMacDoc(){
        return mDailyMacDoc;
    }

    public ArrayList<RecomndRecipe> getRecomndsList(){
        return mRecomndsList;
    }


}
