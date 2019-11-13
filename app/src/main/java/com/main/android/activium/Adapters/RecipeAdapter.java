package com.main.android.activium.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.main.android.activium.R;

import java.util.ArrayList;

public class RecipeAdapter extends ArrayAdapter<String> {

    /**class constructor method*/
    public RecipeAdapter(Activity activity, ArrayList<String> steps){
        super(activity,0, steps);
    }

    //getView() @override that inflates ListView: R.layout.list_item_logged_meals
    @NonNull
    @Override
    public View getView(int position, View convertView,
                        @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView==null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_recipe_ingr,
                    parent, false);
        }

        // Get the Array item
        String ingredient = getItem(position);

        // Set text view
        TextView ingr_body = listItemView.findViewById(R.id.ingr_body);
        ingr_body.setText(ingredient);


        // return configured list item
        return listItemView;
    }


}
