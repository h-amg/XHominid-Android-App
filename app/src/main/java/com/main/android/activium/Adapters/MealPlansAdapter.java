package com.main.android.activium.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.main.android.activium.DataClasses.RecomndRecipe;
import com.main.android.activium.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MealPlansAdapter extends BaseExpandableListAdapter{

    private final String TAG = "MealPlansAdapter";

    private Context mCotext;
    private List<String> mListDataHeader; // header titles
    // child data in format of header title, child text
    private HashMap<String, ArrayList<RecomndRecipe>> mMealRecipes;


    public MealPlansAdapter(Context context, List<String> listDataHeader,
                            HashMap<String, ArrayList<RecomndRecipe>> mealRecipes) {
        this.mCotext = context;
        this.mListDataHeader = listDataHeader;
        this.mMealRecipes = mealRecipes;
        /*Log.d("mealRecipes","RecomndsAdapter triggered");*/
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.mMealRecipes.get(this.mListDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final RecomndRecipe mealRecipe = (RecomndRecipe) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mCotext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_meal_plans, null);
        }


        // ger values
        String imgUrl = mealRecipe.getImgUrl();
        /*Log.d("Recomnds/imgUrl","imgUrl: " + imgUrl);*/
        String text = mealRecipe.getRecomnd();
        //Log.d("Recomnds/text","Text: " + text);
        boolean isPrepped = mealRecipe.getIsPreped();
        /*Log.d(TAG,"isPrepped: " + isPrepped);*/
        boolean isEaten = mealRecipe.getEaten();
        /*Log.d(TAG,"isEaten: " + isEaten);*/

        // set values to views
        ImageView imageView = convertView.findViewById(R.id.meal_plan_meal_img);
        if (imgUrl != null){
            Glide.with(mCotext).load(imgUrl).apply(new RequestOptions()
                    .override(65, 65)).apply(RequestOptions.centerCropTransform())
                    .into(imageView);
        }else{
            Log.e(TAG, "error loading reomnd image, url is null");
        }

        TextView body = convertView.findViewById(R.id.meal_plan_meal_name);
        body.setText(text);

        // lick views
        ImageView preppedChk = convertView.findViewById(R.id.mealPlans_recomnd_preped_chk);
        ImageView eatenChk = convertView.findViewById(R.id.mealPlans_recomnd_eaten_chk);

        View preppedChkPlaceHolder = convertView.findViewById(R.id.mealPlans_recomnd_preped_chk_place_holder);
        if (isPrepped && !isEaten){
            /*Log.d(TAG, "eaten check set visible");*/
            preppedChkPlaceHolder.setVisibility(View.GONE);
            eatenChk.setVisibility(View.GONE);
            preppedChk.setVisibility(View.VISIBLE);
        }else if (isEaten) {
            /*Log.d(TAG, "prepped eaten set visible");*/
            preppedChkPlaceHolder.setVisibility(View.GONE);
            preppedChk.setVisibility(View.GONE);
            eatenChk.setVisibility(View.VISIBLE);
        }else{
            /*Log.d(TAG, "all checks set invisible");*/
            preppedChk.setVisibility(View.GONE);
            eatenChk.setVisibility(View.GONE);
            preppedChkPlaceHolder.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mMealRecipes.get(this.mListDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mCotext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.header_expand_meal_plan__list, null);
        }

        // set header text
        TextView headerTextViwe = convertView
                .findViewById(R.id.meal_plan_header_title);
        headerTextViwe.setText(headerTitle);

        // set indicator state
        final ImageView indicator = convertView.findViewById(R.id.meal_plan_group_indicator);
        indicator.setSelected(isExpanded);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
