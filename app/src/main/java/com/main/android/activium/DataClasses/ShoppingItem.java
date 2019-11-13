package com.main.android.activium.DataClasses;


import android.util.Log;

import org.bson.Document;

import java.util.Date;

public class ShoppingItem {

    // Initiate variables
    private String    mName;
    private String    mMeasure;
    private Double    mQuantity;



    //custom constructor for testing
    public ShoppingItem(Document shoppingItemsDoc){
        try{
            mName = shoppingItemsDoc.getString("name");
        }catch (Exception e){
            Log.e("ShoppingItem", "Error retrieving mName from shoppingItems document");
        }
        try{
            mMeasure = shoppingItemsDoc.getString("measure");
        }catch (Exception e){
            Log.e("ShoppingItem", "Error retrieving mMeasure from shoppingItems document");
        }
        try{
            mQuantity = shoppingItemsDoc.getDouble("quantity");
        }catch (Exception e){
            Log.e("ShoppingItem", "Error retrieving mQuantity from shoppingItems document");
        }
    }

    public String getmName(){
        return mName;
    }

    public String getmMeasure(){
        return mMeasure;
    }

    public Double getmQuantity(){
        return mQuantity;
    }


}
