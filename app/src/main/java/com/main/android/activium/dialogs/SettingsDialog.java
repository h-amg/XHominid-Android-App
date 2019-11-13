package com.main.android.activium.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.main.android.activium.R;


public class SettingsDialog extends AppCompatDialogFragment {
    private static  String TAG = "Setting Dialog Activity";
    private static final int logperiod = 100;
    private static final int dietStatperiod = 200;

    private Integer period;
    private DialogLisstner listener;
    private Button maddBtn;
    private Button msubtractBtn;
    private TextView periodTxtView;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_settings_dialog, null);

        builder.setView(view)
                .setTitle("Set reminder frequency")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //hide dialog
                        dismiss();
                    }
                })
                .setPositiveButton("update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (listener.dialogIdentifier()) {
                            case logperiod:
                                listener.updateLogPeriod(period);
                                //TODO: SAVE UPDATED REMINDER FREQUENCY FOR LOGGING DIET
                                break;
                            case dietStatperiod:
                                listener.updateDtStatPeriod(period);
                                //TODO: SAVE UPDATED REMINDER FREQUENCY FOR DIET STATS
                                break;
                            default:
                        }
                    }
                });

        maddBtn = view.findViewById(R.id.addBtn);
        msubtractBtn = view.findViewById(R.id.minusBtn);
        periodTxtView = view.findViewById(R.id.peiod_value);

        /*switch (listener.dialogIdentifier()) {
            case logperiod:
                //TODO: GET CURRENT USER MACRO LOGGING REMINDER PERIOD
                break;
            case dietStatperiod:
                //TODO: GET CURRENT USER MACRO LOGGING REMINDER PERIOD
                break;
            default:
                Log.d(TAG, "No dialog identifier matched @period value");
        }*/

        period = 5; //TODO: GET CURRENT USER REMINDER FREQUENCY PERIOD VALUE

        maddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (period >= 1){
                    period += 1;
                    String textUpdate = period + " Hours";
                    periodTxtView.setText(textUpdate);
                }
            }
        });

        msubtractBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (period > 1) {
                    period -= 1;
                    String textUpdate = period + " Hours";
                    periodTxtView.setText(textUpdate);
                }
            }
        });

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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DialogLisstner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement DialogListener");
        }
    }

    public interface DialogLisstner {
        void updateLogPeriod(int duration);
        void updateDtStatPeriod(int duration);
        int dialogIdentifier();
    }

}
