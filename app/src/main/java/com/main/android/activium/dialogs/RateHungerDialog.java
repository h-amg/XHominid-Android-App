package com.main.android.activium.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.main.android.activium.R;


public class RateHungerDialog extends AppCompatDialogFragment {

    private Button one, two, three, four, five;
    private rateHungerInterface listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // setup inflator
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.hunger_rating_dialog, null, false);
        // build dialog view
        builder.setView(view);


        // Assign views to variables  view
        one = view.findViewById(R.id.number_1_hunger);
        two = view.findViewById(R.id.number_2_hunger);
        three = view.findViewById(R.id.number_3_hunger);
        four = view.findViewById(R.id.number_4_hunger);
        five = view.findViewById(R.id.number_5_hunger);

        // rating button click listener
        one.setOnClickListener(this::rate);
        two.setOnClickListener(this::rate);
        three.setOnClickListener(this::rate);
        four.setOnClickListener(this::rate);
        five.setOnClickListener(this::rate);

        // create feedback dialog
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

    private void rate(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.number_1_hunger:
                listener.rateHunger(1);
                dismiss();
                break;
            case R.id.number_2_hunger:
                listener.rateHunger(2);
                dismiss();
                break;
            case R.id.number_3_hunger:
                listener.rateHunger(3);
                dismiss();
                break;
            case R.id.number_4_hunger:
                listener.rateHunger(4);
                dismiss();
                break;
            case R.id.number_5_hunger:
                listener.rateHunger(5);
                dismiss();
                break;
            default:
                Toast.makeText(getContext(), "there was an error submitting your rating", Toast.LENGTH_LONG).show();
                dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (rateHungerInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement rateHungerInterface");
        }
    }

    public interface rateHungerInterface {
        void rateHunger(int rating);
    }
}
