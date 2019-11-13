package com.main.android.activium.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.main.android.activium.DataClasses.ShoppingItem;
import com.main.android.activium.R;

import java.util.ArrayList;

public class ShoppingListAdapter extends ArrayAdapter<ShoppingItem> {

    /**class constructor method*/
    public ShoppingListAdapter(Activity activity, ArrayList<ShoppingItem> shoppingItems){
        super(activity,0, shoppingItems);
    }

    //getView() @override that inflates ListView: R.layout.list_item_logged_meals
    @NonNull
    @Override
    public View getView(int position, View convertView,
                        @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView==null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_shopping,
                    parent, false);
        }



        /**Get the Array item (LoggedMeal object type) from the ArrayList
         * adapted to the list view*/
        ShoppingItem shoppingItem = getItem(position);




        /**Declare and find  TextViews*/
        TextView quantityAndMeasureView = listItemView.findViewById(R.id.quantity_and_measure);
        TextView itemNameView = listItemView.findViewById(R.id.item_name);


        /// set views content variables
        String itemName  = shoppingItem.getmName();
        String measure  = shoppingItem.getmMeasure();
        Double quantity  = shoppingItem.getmQuantity();

        String quantityMeasure = quantity + " " + measure;


        //Set view values
        quantityAndMeasureView.setText(quantityMeasure);
        itemNameView.setText(itemName);


        // return configured list item
        return listItemView;
    }


}
