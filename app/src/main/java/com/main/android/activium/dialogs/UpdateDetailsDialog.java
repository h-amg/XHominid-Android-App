package com.main.android.activium.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.MainActivity;
import com.main.android.activium.R;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import eu.amirs.JSON;


public class UpdateDetailsDialog extends AppCompatDialogFragment {


    private String goal;  // "gain" or "lose"
    private Double activityModifier;  // light = 1.3, very light = 1.55, moderate = 1.65, heavy = 1.8, very heavy 2
    private Double  weight;
    private String  weightUnit;
    private Spinner weightUnitSpinner;
    private Spinner goalSpinner;
    private Spinner activitySpinner;
    private EditText weightInputValue;
    private Button updateBtn;
    private String uId;
    private UpdateDetailsInterface listener;
    private int attempts = 0;

    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection usersCollection;
    final StitchUser user = stitchClient.getAuth().getUser();

    // server api url
    final static String uri = "server_url";

    // updating diet progress dialog
    ProgressDialog updateDietProg;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // setup inflator
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_update_details, null);
        // build dialog view
        builder.setView(view);

        // Assign views to variables  view
        updateBtn = view.findViewById(R.id.send_update_btn);

        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        usersCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        // get user id
        if (user != null){
            uId = user.getId();
        }

        // weight
        List<String> weightUnitList = new ArrayList<String>();
        weightUnitList.add("Kilos");
        weightUnitList.add("Pounds");
        ArrayAdapter<String> weightUnitArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.support_simple_spinner_dropdown_item, weightUnitList);
        weightUnitSpinner = view.findViewById(R.id.weight_unit_update);
        weightUnitSpinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        weightUnitSpinner.setDropDownVerticalOffset(weightUnitSpinner.getHeight());
        weightUnitSpinner.setAdapter(weightUnitArrayAdapter);
        // weight value (Double)
        weightInputValue = view.findViewById(R.id.weight_input_update);
        // server diet update dialog
        updateDietProg = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);

        // Goal
        List<String> goalList = new ArrayList<String>();
        goalList.add("Lose weight");
        goalList.add("Gain weight");
        goalList.add("Maintain weight");
        ArrayAdapter<String> goalArrayAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.support_simple_spinner_dropdown_item, goalList);
        goalSpinner = view.findViewById(R.id.goal_update);
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
        activitySpinner = view.findViewById(R.id.activity_update);
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

        // send feedback button on click listener
        updateBtn.setOnClickListener(this::updateDetails);

        // create feedback dialog
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set transparent background
        if (this.getDialog() != null){
            if (this.getDialog().getWindow() != null) {
                this.getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }

    public void updateDetails(View view) {
        listener.hideKeyboardUpdate();
        weightUnit = weightUnitSpinner.getSelectedItem().toString();
        if (validate()) {
            if (weightUnit == "Kilos") {
                weight = Double.valueOf(weightInputValue.getText().toString());
            } else {
                // weight in pounds converter to kg
                weight = Double.valueOf(weightInputValue.getText().toString()) * 0.453592;
            }
        }
        uploadFeedback(weight, goal, activityModifier);
    }

    private void uploadFeedback (Double weight, String goal, Double activityModifier){

        final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Updating your details...");
        progressDialog.show();

        Document filterDoc = new Document("uId", uId);
        Document updateDoc = new Document()
                .append("$set", new Document()
                        .append("weight", weight)
                        .append("goal", goal)
                        .append("activity_modifier", activityModifier)
                );

        final Task<RemoteUpdateResult> updateTask = usersCollection.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener<RemoteUpdateResult>() {
            @Override
            public void onComplete(@NonNull Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    /*long numModified = task.getResult().getModifiedCount();*/
                    /*Log.d("UpdateDetails", "Successfully match user document." +
                            " no. matched: " + numModified);*/
                    makeServerCall(uId, "set_diet", null, null,
                            null, null, 0);
                    progressDialog.dismiss();
                    listener.detailsSuccessUpdate();
                    dismiss();
                } else {
                    if (attempts < 5) {
                        uploadFeedback(weight, goal, activityModifier); // recursive retry
                        attempts += 1;
                    }else{
                        Log.e("UpdateDetails", "failed to update userDocs document with: "
                                , task.getException());
                        listener.detailsFailureUpdate();
                        progressDialog.dismiss();
                        dismiss();
                    }
                }
            }
        });
    }

    // Query backend server
    public void makeServerCall(String uId, String intent, String food, String measure,
                               Double quantity, String recipeId, int dayHour) {
        /*Log.d("MainActiviy", "fetchEarthquakeData initialized");*/

        updateDietProg.setIndeterminate(true);
        updateDietProg.setMessage("Updating your diet...");
        updateDietProg.show();

        // Create URL object
        URL url = makeUrl(uri);

        // Perform HTTP request to the URL and receive a JSON response back
        try {
            makeHTTPRequest(url, uId, intent, food, measure, quantity, recipeId, dayHour);
        } catch (IOException e) {
            listener.detailsFailureUpdate();
            updateDietProg.dismiss();
            dismiss();
            Log.e("MainActiviy", "Error making Http request to server", e);
        }

    }

    private static URL makeUrl(String urlString){
        URL url = null;
        try {
            url = new URL(urlString);
        }catch(MalformedURLException e){
            Log.e("MainActiviy", "Problem making URL object from string", e);
        }
        return url;
    }

    private void makeHTTPRequest(URL url,String uId, String intent, String food, String measure, Double quantity, String recipeId, int dayHour) throws IOException {
        // handle asynchronously
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids) {
                String jsonResponse = "";
                HttpURLConnection urlConnection= null;
                InputStream inputStream = null;

                // If the URL is null, then return early.
                if (url == null) {
                    return jsonResponse;
                }

                // build json object with  query parameters
                String jsonString = "{\"response\": \"\"}";

                /*Log.d("MainActiviy", "uriString: " + jsonString);*/
                JSON jsonParam = new JSON(jsonString);

                try{
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(10000 /* milliseconds */);
                    urlConnection.setConnectTimeout(15000 /* milliseconds */);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(jsonParam.toString());
                    Log.i(MainActivity.class.toString(), jsonParam.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    urlConnection.connect();
                    // If the request was successful (response code 200),
                    // then read the input stream and parse the response.
                    if (urlConnection.getResponseCode() == 200) {
                        inputStream = urlConnection.getInputStream();
                        jsonResponse = readFromStream(inputStream);
                        /*Log.d("MainActiviy", "jsonResponse: " + jsonResponse);*/
                    } else {
                        Log.e("MainActiviy", "Error response code: " + urlConnection.getResponseCode());
                        listener.detailsFailureUpdate();
                        updateDietProg.dismiss();
                        dismiss();
                    }
                } catch (IOException e) {
                    Log.e("MainActiviy", "Problem retrieving the earthquake JSON results.", e);
                    listener.detailsFailureUpdate();
                    updateDietProg.dismiss();
                    dismiss();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }catch(Exception e){
                            Log.e("MainActiviy", "inputStream error: " + e);
                            listener.detailsFailureUpdate();
                            updateDietProg.dismiss();
                            dismiss();
                        }
                    }
                }
                return jsonResponse;
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                JSON jsonResponse = new JSON(response);
                String responseString;
                try {
                    responseString = jsonResponse.getJsonObject().getString("response");
                }catch (Exception e){
                    responseString = null;
                    Log.e("UpdateDetails", "Error processing server json response");
                    listener.detailsFailureUpdate();
                    updateDietProg.dismiss();
                    dismiss();
                }
                //Log.d("MainActiviy", "server response: " + responseString);
                listener.detailsSuccessUpdate();
                updateDietProg.dismiss();
                dismiss();
            }
        }.execute();
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (UpdateDetailsInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement UpdateDetails Interface");
        }
    }

    public interface UpdateDetailsInterface {
        void detailsSuccessUpdate();
        void detailsFailureUpdate();
        void hideKeyboardUpdate();
    }

    private boolean validate(){
        boolean valid = true;
        if (weightInputValue.getText().toString().isEmpty()){
            weightInputValue.setError("field is empty!");
            valid = false;
        }
        return valid;
    }
}
