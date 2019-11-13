package com.main.android.activium.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.main.android.activium.R;

import java.util.ArrayList;

public class RecordsdaysAdapter extends ArrayAdapter<String> {

    LayoutInflater flater;

    public RecordsdaysAdapter(Activity context, int resouceId, int textviewId, ArrayList<String> Dayss){
        super(context,resouceId,textviewId, Dayss);
        flater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowview = convertView;
        if(rowview == null){
            rowview = flater.inflate(R.layout.days_spinner_item,null,true);
        }

        String rowItem = getItem(position);

        TextView txtTitle = rowview.findViewById(R.id.item_label);
        txtTitle.setText(rowItem);

        return rowview;
    }
}
