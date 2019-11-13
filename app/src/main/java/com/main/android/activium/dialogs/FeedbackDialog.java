package com.main.android.activium.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.main.android.activium.R;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;

import java.util.Calendar;


public class FeedbackDialog extends AppCompatDialogFragment {


    private EditText editText;
    private Button sendBtm;
    private SpinKitView progresBar;
    private LinearLayout inputView;
    private String uId;
    private InterFeedBack listener;
    private int attempts = 0;


    // MongoDB
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection feedBackCollection;
    final StitchUser user = stitchClient.getAuth().getUser();


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // setup inflator
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_feedback_dialog, null);
        // build dialog view
        builder.setView(view);

        // Assign views to variables  view
        editText = view.findViewById(R.id.feedback_editText);
        sendBtm = view.findViewById(R.id.send_feedback_btn);
        progresBar = view.findViewById(R.id.feedBAck_prog);
        inputView = view.findViewById(R.id.input_view);

        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "app_name");
        feedBackCollection = mongoDBClient.getDatabase("databae_name")
                .getCollection("collection_name");

        // get user id
        if (user != null){
            uId = user.getId();
        }
        // send feedback button on click listener
        sendBtm.setOnClickListener(this::sendFeedBack);

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

    public void sendFeedBack(View view) {
        listener.hideKeyboard();
        String feeddBAck = editText.getText().toString();
        inputView.setVisibility(View.GONE);
        progresBar.setVisibility(View.VISIBLE);
        editText.setText("");

        uploadFeedback(feeddBAck);
    }

    private void uploadFeedback (String feeddBAck){

        Document feedBackDoc = new Document()
                .append("uId", uId)
                .append("feedback", feeddBAck)
                .append("timestamp", Calendar.getInstance().getTime());

        final Task <RemoteInsertOneResult> insertTask = feedBackCollection.insertOne(feedBackDoc);
        insertTask.addOnCompleteListener(new OnCompleteListener <RemoteInsertOneResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    /*Log.d("FeedBack Dialog", String.format("successfully inserted userDoc" +
                                    " with id %s", task.getResult().getInsertedId()));*/
                    listener.triggerSuccessToast();
                    dismiss();
                } else {
                    Log.e("FeedBack Dialog", "failed to insert userDoc with: ",
                            task.getException());
                    if (attempts < 5) {
                        uploadFeedback(feeddBAck); // recursive retry
                        attempts += 1;
                    }else{
                        listener.triggerFailureToast();
                        dismiss();
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (InterFeedBack) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement FeedBack interface");
        }
    }

    public interface InterFeedBack {
        void triggerSuccessToast();
        void triggerFailureToast();
        void hideKeyboard();
    }
}
