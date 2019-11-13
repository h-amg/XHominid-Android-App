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

import com.main.android.activium.DataClasses.RecomndRecipe;
import com.main.android.activium.R;


public class ConfirmEatenDialog extends AppCompatDialogFragment {

    private Button yesBtn, noBtn;
    private confirmEatenInterface listener;
    private RecomndRecipe mRecomndRecipe;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // setup inflator
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.confirm_eaten_dialog, null);
        // build dialog view
        builder.setView(view);

        // Assign views to variables  view
        yesBtn = view.findViewById(R.id.yes_confirm_eaten_btn);
        noBtn = view.findViewById(R.id.no_confirm_eaten_btn);


        // send confirm button on click listener
        yesBtn.setOnClickListener(this::confirmEaten);
        // send decline button on click listener
        noBtn.setOnClickListener(this::decline);

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

    public void setmRecomndRecipe(RecomndRecipe recomndRecipe){
        mRecomndRecipe = recomndRecipe;
    }


    private void confirmEaten(View view) {
        if (mRecomndRecipe != null) {
            listener.confirmEaten(mRecomndRecipe);
        }else{
            Toast.makeText(getActivity(), "Error occured", Toast.LENGTH_LONG).show();
        }
        dismiss();
    }

    private void decline(View v){
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (confirmEatenInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement confirmEatenInterface");
        }
    }

    public interface confirmEatenInterface {
        void confirmEaten(RecomndRecipe recomndRecipe);
    }
}
