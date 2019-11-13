package com.main.android.activium.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.R;
import com.main.android.activium.DataClasses.Recipe;
import com.main.android.activium.Adapters.RecipeAdapter;
import com.main.android.activium.DataClasses.RecomndRecipe;
import com.main.android.activium.util.DayBoundaries;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MealRecipeDialog extends Dialog {



    private ImageView mealImage;
    private TextView mealName;
    private ScrollView recipe_descript_view;
    private ListView stepsList;
    private String mRecipeIId;
    private RecomndRecipe mRecomndRecipe;
    private SpinKitView progerssBar;
    private LinearLayout contentView;
    private LinearLayout stepsView;
    private Button startNxtBtn, havingLaterbtn;
    private ImageButton close;
    private TextView descript;
    private List<String> steps;
    private int currStep;
    private TextView stepNumber;
    private TextView stepTxtView;


    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection recipesCollections;

    // set handle recipe listener
    private handleRecipe handleRecipeListener;

    // ingreditnsAdapter
    private RecipeAdapter ingreditnsAdapter;

    // steps transition
    private int shortAnimDrtn;

    private Date selectedDate = Calendar.getInstance().getTime();

    private final String TAG = "RecipeDuialog";



    public MealRecipeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        try {
            handleRecipeListener = (handleRecipe) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.toString() +
                    "must implement handleRecipe. error: " + e);
        }
    }

    @Override
    public void onBackPressed() {
        /*Log.d(TAG, "step @close before: " + currStep);*/
        if (currStep == 1 && contentView.getVisibility() == View.GONE){
            /*Log.d(TAG, "step @close @zero: " + currStep);*/
            close.setImageResource(R.drawable.outline_close_black_24);
            startNxtBtn.setText(R.string.start_prep);
            startNxtBtn.setTextSize(18);
            // set step number
            currStep -= 1; // index value adjusted to start from 1
            /*Log.d(TAG, "step @close @zero after: " + currStep);*/
            // transition
            stepsView.setVisibility(View.GONE);
            transition(contentView);
        }else if (currStep > 1){
            close.setImageResource(R.drawable.outline_arrow_back_black_24);
            // set meal prep btn visibility
            if (currStep == steps.size() - 1){
                startNxtBtn.setTextSize(18);
                startNxtBtn.setText(R.string.next_step);
                startNxtBtn.setVisibility(View.VISIBLE);
                havingLaterbtn.setVisibility(View.GONE);
            }
            // set step number
            currStep -= 1; // index value adjusted to start from 1
            /*Log.d(TAG, "step @close after: " + currStep);*/
            // set step view content
            stepNumber.setText(String.valueOf(currStep));
            /*Log.d(TAG, "@close steps.get(currStep): " + steps.get(currStep - 1));*/
            stepTxtView.setText(steps.get(currStep - 1)); // values starts from 0 (adjusted for index value + 1)
            // transition
            stepsView.setVisibility(View.GONE);
            transition(stepsView);
        }else{
            dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_recipe_dialog);


        // link view
        mealImage = findViewById(R.id.recipe_image);
        mealName = findViewById(R.id.meal_name);
        recipe_descript_view = findViewById(R.id.recipe_descript_view);
        stepsList = findViewById(R.id.ingredients_list);
        progerssBar = findViewById(R.id.recipe_progress_bar);
        contentView = findViewById(R.id.content_view);
        stepsView = findViewById(R.id.steps_content);
        startNxtBtn = findViewById(R.id.start_next_btn);
        havingLaterbtn = findViewById(R.id.having_it_later_btn);
        close = findViewById(R.id.recipe_dialog_close_btn);
        descript = findViewById(R.id.recipe_description);
        stepNumber = findViewById(R.id.step_number);
        stepTxtView = findViewById(R.id.step_text);

        // enable description vertical scroll
        descript.setVerticalScrollBarEnabled(true);

        // Retrieve and cache the system's default "short" animation time.
        if (getOwnerActivity() != null) {
            shortAnimDrtn = getOwnerActivity().getResources().getInteger(
                    android.R.integer.config_shortAnimTime);
        }else{
            Log.e(TAG, "Own activity is null @shortAnimDrtn");
        }

        //set toolbar
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Log.d(TAG, "step @close before: " + currStep);*/
                if (currStep == 1 && contentView.getVisibility() == View.GONE){
                    /*Log.d(TAG, "step @close @zero: " + currStep);*/
                    close.setImageResource(R.drawable.outline_close_black_24);
                    startNxtBtn.setTextSize(18);
                    startNxtBtn.setText(R.string.start_prep);
                    startNxtBtn.setVisibility(View.VISIBLE);
                    // set step number
                    currStep -= 1; // index value adjusted to start from 1
                    /*Log.d(TAG, "step @close @zero after: " + currStep);*/
                    // transition
                    stepsView.setVisibility(View.GONE);
                    transition(contentView);
                }else if (currStep > 1){
                    close.setImageResource(R.drawable.outline_arrow_back_black_24);
                    // set meal prep btn visibility
                    if (currStep == steps.size() - 1){
                        startNxtBtn.setText(R.string.next_step);
                        startNxtBtn.setTextSize(18);
                        startNxtBtn.setVisibility(View.VISIBLE);
                        havingLaterbtn.setVisibility(View.GONE);
                    }
                    // set step number
                    currStep -= 1; // index value adjusted to start from 1
                    /*Log.d(TAG, "step @close after: " + currStep);*/
                    // set step view content
                    stepNumber.setText(String.valueOf(currStep));
                    /*Log.d(TAG, "@close steps.get(currStep): " + steps.get(currStep - 1));*/
                    stepTxtView.setText(steps.get(currStep - 1)); // values starts from 0 (adjusted for index value + 1)
                    // transition
                    stepsView.setVisibility(View.GONE);
                    transition(stepsView);
                }else{
                    dismiss();
                }
            }
        });





        // load ui data
        fetchRecipeData();

        // TODO: Set loading progressbar bar on start
        // TODO: Retrieve recipe document and update ui
        // TODO: Add eaten button and close button
        /*saveBtn = view.findViewById(R.id.save_btn);

        saveBtn = view.findViewById(R.id.save_btn);
        //  Save Button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                age = (int) ageSpinner.getSelectedItem();
                weightUnit = weightUnitSpinner.getSelectedItem().toString();
                heightUnit = heighttUnitSpinner.getSelectedItem().toString();

                if (validate()) {
                    if (weightUnit == "Kilos") {
                        weight = Double.valueOf(weightInputValue.getText().toString());
                    } else {
                        // weight in pounds converter to kg
                        weight = Double.valueOf(weightInputValue.getText().toString()) * 0.453592;
                    }

                    if (heightUnit == "Cm") {
                        height = Double.valueOf(heightInputValueCm.getText().toString());
                    } else {
                        // height in foot/inch converted to cm
                        Double foot = Double.valueOf(heightInputValueFt.getText().toString());
                        Double inch = Double.valueOf(heightInputValueIn.getText().toString());
                        height = (foot * 30.48) + (inch * 2.54);
                    }

                    listener.createUserDoc(activityModifier, age, gender, goal, height, weight);
                    dismiss();
                }
            }
        });*/

       /* eatenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRecipeListener.setEaten(mRecomndRecipe);
                dismiss();
            }
        });*/

        startNxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Log.d(TAG, "step @startNxtBtn before: " + currStep);*/
                if(currStep == 0){
                    close.setImageResource(R.drawable.outline_arrow_back_black_24);
                    startNxtBtn.setText(R.string.next_step);
                    startNxtBtn.setTextSize(18);
                    currStep += 1; // index value adjusted to start from 1
                    /*Log.d(TAG, "step @startNxtBtn @zero after: " + currStep);*/
                    // set step view content
                    stepNumber.setText(String.valueOf(currStep)); // values starts from 1
                    /*Log.d(TAG, "@startNxtBtn steps.get(currStep): " + steps.get(currStep - 1));*/
                    stepTxtView.setText(steps.get(currStep - 1)); // values starts from 0 (adjusted for index value + 1)
                    // transition
                    contentView.setVisibility(View.GONE);
                    transition(stepsView);
                }else if (currStep > 0 && currStep < steps.size() - 1){
                    currStep += 1; // index value adjusted to start from 1
                    /*Log.d(TAG, "step @startNxtBtn @>0 after: " + currStep);*/
                    // set step view content
                    stepNumber.setText(String.valueOf(currStep)); // values starts from 1
                    /*Log.d(TAG, "@startNxtBtn steps.get(currStep): " + steps.get(currStep - 1));*/
                    stepTxtView.setText(steps.get(currStep - 1)); // values starts from 0 (adjusted for index value + 1)
                    // transition
                    stepsView.setVisibility(View.GONE);
                    transition(stepsView);
                    // set meal prep btn visibility
                    // do nothing if the meal is not prepped for today
                    Date currDate = Calendar.getInstance().getTime();
                    DayBoundaries today = new DayBoundaries(currDate);
                    /*Log.d(TAG, "selected date: " + selectedDate + "today start:" +
                            " " + today.getDayStart() + "today end: " + today.getDayEnd());*/
                    if (currStep == steps.size() - 1 && selectedDate.compareTo(today.getDayStart())
                            >= 0 && selectedDate.compareTo(today.getDayEnd()) <= 0) {
                        startNxtBtn.setText(R.string.eating_now);
                        startNxtBtn.setTextSize(12);
                        havingLaterbtn.setVisibility(View.VISIBLE);
                    }
                    else if (currStep == steps.size() - 1 && selectedDate.compareTo(today.getDayStart())
                            < 0 || selectedDate.compareTo(today.getDayEnd()) >= 0){
                        startNxtBtn.setVisibility(View.GONE);
                    }
                    else{
                        startNxtBtn.setVisibility(View.VISIBLE);
                        havingLaterbtn.setVisibility(View.GONE);
                    }
                }else if (currStep == steps.size() - 1){
                    handleRecipeListener.setEaten(mRecomndRecipe);
                    Toast.makeText(getOwnerActivity(), "Enjoy!", Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        });

        havingLaterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRecipeListener.setPrepForLater(mRecomndRecipe);
                dismiss();
            }
        });

    }




    public void setRecipe(RecomndRecipe recipe) {
        mRecipeIId = recipe.getRecipeId();
        /*Log.d(TAG, "mRecipeIId: " + mRecipeIId);*/
        mRecomndRecipe = recipe;
    }

    private void fetchRecipeData(){
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        recipesCollections = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        Document filterDoc = new Document().append("recipeid", mRecipeIId);
        // fetch user diet data
        final Task<Document> findTask = recipesCollections.find(filterDoc).limit(1).first();
        findTask.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull Task <Document> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null) {
                        Log.e("loadRecipeId", "Could not find any matching recipe documents");
                    } else {
                        Log.d("loadRecipeId", String.format("successfully found recipe document: %s",
                                task.getResult().toString()));
                        // fetch user's today's macros
                        setUiData(new Recipe(task.getResult()));
                    }
                } else {
                    Log.e("loadRecipeId", "failed to find diet document with: ", task.getException());
                    if (getOwnerActivity() != null) {
                        Toast.makeText(getOwnerActivity(), "Error loading recipe", Toast.LENGTH_LONG).show();
                    }
                    dismiss();
                }
            }
        });
    }

    private void setUiData(Recipe recipe){
        // load recipe photo
        String imageUrl = recipe.getImageUrl();
        if (imageUrl != null){
            // set drawer profile image
            Glide.with(getContext()).load(imageUrl).apply(new RequestOptions()
                    .override(360, 360)).apply(RequestOptions.circleCropTransform())
                    .into(mealImage);
        }else {
            Log.e("recipeImage",
                    "error setting recipe photo. imageUrl value: "  + imageUrl);
        }

        // set recipe name
        String name = recipe.getName();
        if (name != null) {
            mealName.setText(name);
        }else{
            Log.e("recipeName", "error mealName value is null");
        }

        String descriptTxt = recipe.getDescript();
        if (descriptTxt != null){
            descript.setText(descriptTxt);
        }else{
            Log.e("recipeName", "error descript value is null");
        }

        // set instruction steps
        steps = recipe.getInstruct();
        /*Log.d(TAG, "steps: " + steps.toString());*/
        if (steps != null) {
            currStep = 0;
        }else{
            Log.e("recipeInstruct", "step value is null");
        }

        // set ingredients
        List<String> ingredietns = recipe.getIngred();
        if (ingredietns != null && getOwnerActivity() != null) {
            ingreditnsAdapter = new RecipeAdapter(getOwnerActivity(), new ArrayList<String>());
            stepsList.setAdapter(ingreditnsAdapter);
            ingreditnsAdapter.addAll(ingredietns);
        }else{
            Log.e("recipeSteps", "error ingreditnsAdapter or getOwnActivity is null");
        }

        if (steps != null && ingredietns != null && getOwnerActivity() != null){
            //hide progress bar and show content view
            progerssBar.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
            startNxtBtn.setVisibility(View.VISIBLE);
        }else{
            // close dialog
            dismiss();
        }

    }

    public interface handleRecipe{
        void setEaten(RecomndRecipe recipe);
        void setPrepForLater(RecomndRecipe recipe);
    }

    private void transition(View fadeinStep) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        fadeinStep.setAlpha(0f);
        fadeinStep.setVisibility(View.VISIBLE);


        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        fadeinStep.animate()
                .alpha(1f)
                .setDuration(shortAnimDrtn)
                .setListener(null);

    }

    public void setSelctedDate(Date newDate){
        selectedDate = newDate;
    }
}
