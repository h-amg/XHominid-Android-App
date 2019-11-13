package com.main.android.activium.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.main.android.activium.R;

import java.util.ArrayList;
import java.util.List;


public class UserInfoDialog extends AppCompatDialogFragment {



    private String gender;  // "m" or "f"
    private Integer age;
    private String goal;  // "gain" or "lose"
    private Double activityModifier;  // light = 1.3, very light = 1.55, moderate = 1.65, heavy = 1.8, very heavy 2
    private Double  height;
    private Double  weight;
    private String  weightUnit;
    private String  heightUnit;
    private UserInfoListener listener;
    private Button saveBtn;
    private Spinner ageSpinner;
    private Spinner genderSpinner;
    private Spinner weightUnitSpinner;
    private Spinner heighttUnitSpinner;
    private Spinner goalSpinner;
    private Spinner activitySpinner;
    private EditText weightInputValue;
    private EditText heightInputValueCm;
    private EditText heightInputValueFt;
    private EditText heightInputValueIn;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_userinfo_dialog, null);

        builder.setView(view);

        // link save button
        saveBtn = view.findViewById(R.id.save_btn);


        // Age
        List<Integer> ageList = new ArrayList<Integer>();
        for (int i = 1; i <= 100; i++) {
            ageList.add(i);
        }
        ArrayAdapter<Integer> ageArrayAdapter = new ArrayAdapter<Integer>(
                getActivity(), R.layout.support_simple_spinner_dropdown_item, ageList);
        ageArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        ageSpinner = view.findViewById(R.id.age_input);
        ageSpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ageSpinner.setDropDownVerticalOffset(ageSpinner.getHeight());
        ageSpinner.setAdapter(ageArrayAdapter);

        // gender
        List<String> genderList = new ArrayList<String>();
        genderList.add("Male");
        genderList.add("Female");
        ArrayAdapter<String> genderArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.support_simple_spinner_dropdown_item, genderList);
        genderSpinner = view.findViewById(R.id.gender);
        genderSpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        genderSpinner.setDropDownVerticalOffset(genderSpinner.getHeight());
        genderSpinner.setAdapter(genderArrayAdapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        gender = "m";
                        break;
                    case 1:
                        gender = "f";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // weight
        List<String> weightUnitList = new ArrayList<String>();
        weightUnitList.add("Kilos");
        weightUnitList.add("Pounds");
        ArrayAdapter<String> weightUnitArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.support_simple_spinner_dropdown_item, weightUnitList);
        weightUnitSpinner = view.findViewById(R.id.weight_unit);
        weightUnitSpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        weightUnitSpinner.setDropDownVerticalOffset(weightUnitSpinner.getHeight());
        weightUnitSpinner.setAdapter(weightUnitArrayAdapter);
        // weight value (Double)
        weightInputValue = view.findViewById(R.id.weight_input);

        // height
        heightInputValueCm = view.findViewById(R.id.heightCm_input);
        heightInputValueFt = view.findViewById(R.id.heightFt_input);
        heightInputValueFt.setHint("ft");
        heightInputValueIn = view.findViewById(R.id.heightIn_input);
        heightInputValueIn.setHint("in");
        heighttUnitSpinner = view.findViewById(R.id.height_unit);
        heighttUnitSpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        heighttUnitSpinner.setDropDownVerticalOffset(heighttUnitSpinner.getHeight());
        List<String> heightUnitList = new ArrayList<String>();
        heightUnitList.add("Cm");
        heightUnitList.add("Ft/ih");
        ArrayAdapter<String> heightUnitArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.support_simple_spinner_dropdown_item, heightUnitList);
        heighttUnitSpinner.setAdapter(heightUnitArrayAdapter);
        heighttUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    heightInputValueCm.setVisibility(View.VISIBLE);
                    heightInputValueFt.setVisibility(View.GONE);
                    heightInputValueIn.setVisibility(View.GONE);
                }else{
                    heightInputValueCm.setVisibility(View.GONE);
                    heightInputValueFt.setVisibility(View.VISIBLE);
                    heightInputValueIn.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Goal
        List<String> goalList = new ArrayList<String>();
        goalList.add("Lose weight");
        goalList.add("Gain weight");
        goalList.add("Maintain weight");
        ArrayAdapter<String> goalArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.support_simple_spinner_dropdown_item, goalList);
        goalSpinner = view.findViewById(R.id.goal);
        goalSpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        goalSpinner.setDropDownVerticalOffset(goalSpinner.getHeight());
        goalSpinner.setAdapter(goalArrayAdapter);
        goalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        goal = "lose";
                        break;
                    case 1:
                        goal = "gain";
                        break;
                    case 2:
                        goal = "maintain";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Activity
        List<String> activityList = new ArrayList<String>();
        activityList.add("Very Light:  Typical office job");
        activityList.add("Light: Any job where you mostly stand or walk");
        activityList.add("Moderate: Jobs requiring physical activity");
        activityList.add("Heavy: Heavy manual labor");
        activityList.add("Very Heavy: 8+ hrs gard physical activity");
        ArrayAdapter<String> activityArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.support_simple_spinner_dropdown_item, activityList);
        activitySpinner = view.findViewById(R.id.activity);
        activitySpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        activitySpinner.setDropDownVerticalOffset(activitySpinner.getHeight());
        activitySpinner.setAdapter(activityArrayAdapter);
        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        activityModifier = 1.3;
                        break;
                    case 1:
                        activityModifier = 1.55;
                        break;
                    case 2:
                        activityModifier = 1.65;
                        break;
                    case 3:
                        activityModifier = 1.8;
                        break;
                    case 4:
                        activityModifier = 2.0;
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


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
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Disable dismissing the dialog on outside touch
        if (this.getDialog() != null){
            this.getDialog().setCanceledOnTouchOutside(false);
            if (this.getDialog().getWindow() != null) {
                this.getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (UserInfoListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement DialogListener");
        }
    }

    public interface UserInfoListener {
        void createUserDoc(Double activityModifier, int age, String gender, String goal,
                           Double height, Double weight);
    }

    private boolean validate(){

        boolean valid = true;

        if (weightInputValue.getText().toString().isEmpty()){
            weightInputValue.setError("field is empty!");
            valid = false;
        }
        if (heightUnit == "Cm" && heightInputValueCm.getText().toString().isEmpty()){
            heightInputValueCm.setError("field is empty!");
            valid = false;
        }
        if (heightUnit == "Ft/ih" && heightInputValueFt.getText().toString().isEmpty()){
            heightInputValueFt.setError("field is empty!");
            valid = false;
        }

        if (heightUnit == "Ft/ih" && heightInputValueIn.getText().toString().isEmpty()){
            heightInputValueIn.setError("field is empty!");
            valid = false;
        }

        return valid;
    }

}
