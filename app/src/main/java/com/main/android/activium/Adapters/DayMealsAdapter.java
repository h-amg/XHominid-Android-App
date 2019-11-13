package com.main.android.activium.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.main.android.activium.DataClasses.LoggedMeal;
import com.main.android.activium.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DayMealsAdapter extends ArrayAdapter<LoggedMeal> {

    /**class constructor method*/
    public DayMealsAdapter(Activity activity, ArrayList<LoggedMeal> loggedMeals){
        super(activity,0, loggedMeals);
    }

    //getView() @override that inflates ListView: R.layout.list_item_logged_meals
    @NonNull
    @Override
    public View getView(int position, View convertView,
                        @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView==null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_logged_meals,
                    parent, false);
        }



        /**Get the Array item (LoggedMeal object type) from the ArrayList
         * adapted to the list view*/
        LoggedMeal loggedMeal = getItem(position);




        /**Declare and find  TextViews*/
        TextView timeView = listItemView.findViewById(R.id.time);
        TextView mealDisripView = listItemView.findViewById(R.id.mealDescript);
        TextView proteinView = listItemView.findViewById(R.id.protein);
        TextView carbsView = listItemView.findViewById(R.id.carbs);
        TextView fatView = listItemView.findViewById(R.id.fat);


        // format time fetched
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm a");
        Date timeRaw = loggedMeal.getTime();

        /// set views content variables
        String time  = timeFormatter.format(timeRaw);
        String mealDiscrip = loggedMeal.getDescrip();
        String protein = "Protein: " + loggedMeal.getProtein().toString();
        String carbs = "Carbs: " + loggedMeal.getCarbs().toString();
        String fat = "Fat: " + loggedMeal.getFat().toString();


        //Set view values
        timeView.setText(time);
        mealDisripView.setText(mealDiscrip);
        proteinView.setText(protein);
        carbsView.setText(carbs);
        fatView.setText(fat);


        /*// Set the proper background color on the magnitude circle.
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable magnitudeCircle = (GradientDrawable) magView.getBackground();

        // Get the appropriate background color based on the current earthquake magnitude
        int magnitudeColor = getMagnitudeColor(mag);

        // Set the color on the magnitude circle
        magnitudeCircle.setColor(magnitudeColor);*/

        // return configured list item
        return listItemView;
    }


}
