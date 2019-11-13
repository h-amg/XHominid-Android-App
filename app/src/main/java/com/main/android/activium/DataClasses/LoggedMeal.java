package com.main.android.activium.DataClasses;



import android.util.Log;

import org.bson.Document;
import java.util.Date;

public class LoggedMeal {

    // Initiate variables
    private Document  mDoc;
    private Document  mMacrosDoc;
    private Date      mTime;
    private String    mDiscription;
    private String    mCategory;
    private Double    mCalories;
    private Double    mProtein;
    private Double    mCarbs;
    private Double    mFat;



    //custom constructor for testing
    public LoggedMeal(Document mealDoc){
        mDoc = mealDoc;

        try {
            mTime = mealDoc.getDate("timestamp");
            mDiscription = mealDoc.getString("food");
            mMacrosDoc = (Document) mealDoc.get("macros");
        }catch (Exception e){
            Log.e("LoggedMeal", "Error retrieving data from LoggedMeal document");
        }

        try{
            mCalories = mealDoc.getDouble("calories");
        }catch (Exception e){
            try {
                mCalories = Double.valueOf(mealDoc.getInteger("calories"));
            }catch (Exception e1){
                Log.e("LoggedMeal", "Error retrieving data from LoggedMeal document");
            }
        }

        try{
            mProtein = mMacrosDoc.getDouble("protein");
        }catch (Exception e){
            try {
                mProtein = Double.valueOf(mMacrosDoc.getInteger("protein"));
            }catch (Exception e1){
                Log.e("LoggedMeal", "Error retrieving data from LoggedMeal document");
            }
        }

        try{
            mCarbs = mMacrosDoc.getDouble("carbs");
        }catch (Exception e){
            try {
                mCarbs = Double.valueOf(mMacrosDoc.getInteger("carbs"));
            }catch (Exception e1){
                Log.e("LoggedMeal", "Error retrieving data from LoggedMeal document");
            }
        }

        try{
            mFat = mMacrosDoc.getDouble("fat");
        }catch (Exception e){
            try {
                mFat = Double.valueOf(mMacrosDoc.getInteger("fat"));
            }catch (Exception e1){
                Log.e("LoggedMeal", "Error retrieving data from LoggedMeal document");
            }
        }
    }


    public Date getTime(){
        return mTime;
    }

    public String getDescrip(){
        return mDiscription;
    }

    public Double getCalories(){
        return mCalories;
    }

    public Double getProtein(){
        return mProtein;
    }

    public Double getCarbs(){
        return mCarbs;
    }

    public Double getFat(){
        return mFat;
    }

}
