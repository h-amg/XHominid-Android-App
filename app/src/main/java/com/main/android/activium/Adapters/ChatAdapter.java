package com.main.android.activium.Adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.main.android.activium.R;
import com.twilio.chat.Message;

import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {
    final static String TAG = "ChatAdapter";

    Activity activity;
    ArrayList<Message> data;
    String uId;
    public ChatAdapter(Activity activity, ArrayList<Message> data, String uId)
    {
        this.activity = activity;
        this.data = data;
        this.uId = uId;
    }
    @Override
    public int getCount() {
        return data.size();
    }
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        String senderId;
        try {
            Log.e(TAG, "attributes"  + data.get(position).getAttributes().toString());
        }catch (Exception e){
            Log.e(TAG, "error retrieving attributes");
        }
        Log.e(TAG, "author" + data.get(position).getAuthor());
        try {
            senderId = data.get(position).getAttributes().getString("uId");
        }catch (Exception e){
            senderId = null;
            Log.e(TAG, "error retrieving senderId");
        }
        if(senderId == uId)
        {
            view = activity.getLayoutInflater().inflate(R.layout.chat_row_right, null);
        }
        else
        {
            view = activity.getLayoutInflater().inflate(R.layout.chat_row_left, null);
        }
        TextView text = view.findViewById(R.id.text);
        text.setText(data.get(position).getMessageBody());

        return view;
    }
}
