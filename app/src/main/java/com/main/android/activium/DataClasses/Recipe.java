package com.main.android.activium.DataClasses;


import android.util.Log;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Recipe {

    // Initiate variables
    private String mRecipeImageUrl;
    private String mRecipeName;
    private String mRecipeDescript;
    private List recipeSteps;
    private List mInstructions;
    final static Class<? extends List> docClazz = new ArrayList<Document>().getClass();

    //custom constructor for testing
    public Recipe(Document recipe){
        try {
            mRecipeImageUrl = recipe.getString("image");
            /*Log.d("MealRecipe", "mRecomnd: " + mRecomnd);*/
        }catch (Exception e){
            Log.e("Recipe", "Error retrieving mRecipeImageUrl from Recipe document: " + e);
        }
        try {
            recipeSteps = recipe.get("ingredients", docClazz);
            /*Log.d("MealRecipe", "mDate: " + mDate);*/
        }catch (Exception e){
            Log.e("Recipe", "Error retrieving recipeSteps from Recipe document: " + e);
        }
        try {
            mInstructions = recipe.get("instruction", docClazz);
        }catch (Exception e){
            Log.e("Recipe", "Error retrieving mInstructions from Recipe document: " + e);
        }
        try {
            mRecipeName = recipe.getString("recipename");
        }catch (Exception e){
            Log.e("Recipe", "Error retrieving mRecipeName from Recipe document: " + e);
        }
        try {
            mRecipeDescript = recipe.getString("description");
        }catch (Exception e){
            Log.e("Recipe", "Error retrieving mRecipeDescript from Recipe document: " + e);
        }
    }

    public String getImageUrl(){
        return mRecipeImageUrl;
    }

    public String getDescript(){
        return mRecipeDescript;
    }

    // recipe ingredients
    public List<String> getIngred(){
        return recipeSteps;
    }

    public List<String> getInstruct(){
        return mInstructions;
    }

    public String getName(){
        return mRecipeName;
    }



}
