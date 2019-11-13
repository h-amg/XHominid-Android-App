package com.main.android.activium.dialogs;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import com.main.android.activium.R;


public class RateEnergyDialog extends AppCompatDialogFragment {

    private Button one, two, three, four, five;
    private rateEnergyInterface listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // setup inflator
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.energy_rating_dialog, null, false);
        // build dialog view
        builder.setView(view);




        // Assign views to variables  view
        one = view.findViewById(R.id.number_1_energy);
        two = view.findViewById(R.id.number_2_energy);
        three = view.findViewById(R.id.number_3_energy);
        four = view.findViewById(R.id.number_4_energy);
        five = view.findViewById(R.id.number_5_energy);

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
            case R.id.number_1_energy:
                listener.rateEnergy(1);
                dismiss();
                break;
            case R.id.number_2_energy:
                listener.rateEnergy(2);
                dismiss();
                break;
            case R.id.number_3_energy:
                listener.rateEnergy(3);
                dismiss();
                break;
            case R.id.number_4_energy:
                listener.rateEnergy(4);
                dismiss();
                break;
            case R.id.number_5_energy:
                listener.rateEnergy(5);
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
            listener = (rateEnergyInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement rateEnergyInterface");
        }
    }

    public interface rateEnergyInterface {
        void rateEnergy(int rating);
    }
}
