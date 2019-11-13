package com.main.android.activium.DataClasses;


import android.util.Log;

import org.bson.Document;
import java.util.Date;

public class RecomndRecipe {

    // Initiate variables
    private String  mRecomnd;
    private Date    mDate;
    private String mRecipeImgUrl;
    private String mRecipeId;
    private String mRecomndId;
    private boolean mPreped;
    private boolean mBreakfast;
    private boolean mLunch;
    private boolean mDinner;
    private boolean mSnack;
    private boolean mEaten;


    //custom constructor for testing
    public RecomndRecipe(Document mealDoc){

        try {
            mRecomnd = mealDoc.getString("name");
            /*Log.d("MealRecipe", "mRecomnd: " + mRecomnd);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mRecomnd: " + e);
        }
        try {
            mDate = mealDoc.getDate("timeStamp");
            /*Log.d("MealRecipe", "mDate: " + mDate);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mDate: " + e);
        }
        try {
            mRecipeId = mealDoc.getString("recipeid");
            /*Log.d("MealRecipe", "mRecipeId: " + mRecipeId);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mRecipeId: " + e);
        }
        try {
            mRecomndId = mealDoc.getObjectId("_id").toString();
            /*Log.d("MealRecipe", "mRecomndId: " + mRecomndId);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mRecomndId: " + e);
        }
        try {
            mRecipeImgUrl = mealDoc.getString("imgUrl");
            /*Log.d("MealRecipe", "mRecipeImgUrl: " + mRecipeImgUrl);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mRecipeImgUrl: " + e);
        }
        try {
            mPreped = mealDoc.getBoolean("isPreped", false);
            Log.d("MealRecipe", "mPreped: " + mPreped);
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mPreped: " + e);
        }
        try {
            mBreakfast = mealDoc.getBoolean("breakfast", false);
            /*Log.d("MealRecipe", "mBreakfast: " + mBreakfast);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mBreakfast: " + e);
        }
        try {
            mLunch = mealDoc.getBoolean("lunch", false);
            /*Log.d("MealRecipe", "mLunch: " + mLunch);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mLunch: " + e);
        }
        try {
            mDinner = mealDoc.getBoolean("dinner", false);
            /*Log.d("MealRecipe", "mDinner: " + mDinner);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mDinner: " + e);
        }
        try {
            mSnack = mealDoc.getBoolean("snack", false);
            /*Log.d("MealRecipe", "mSnack: " + mSnack);*/
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mSnack: " + e);
        }
        try {
            mEaten = mealDoc.getBoolean("eaten", false);
            Log.d("MealRecipe", "mEaten: " + mEaten);
        }catch (Exception e){
            Log.e("RecomndRecipe", "Error retrieving data from RecomndRecipe document" +
                    " @mEaten: " + e);
        }
    }

    public Date getDate(){
        return mDate;
    }

    public String getRecomnd(){
        return mRecomnd;
    }

    public String getRecipeId(){
        return mRecipeId;
    }

    public String getRecomndId(){
        return mRecomndId;
    }

    public String getImgUrl(){
        return mRecipeImgUrl;
    }

    public boolean getIsPreped(){
        return mPreped;
    }

    public boolean getIsBreakfast(){
        return mBreakfast;
    }

    public boolean getIsLunch(){
        return mLunch;
    }

    public boolean getIsDinner(){
        return mDinner;
    }

    public boolean getIsSnack(){
        return mSnack;
    }

    public boolean getEaten(){
        return mEaten;
    }
}
