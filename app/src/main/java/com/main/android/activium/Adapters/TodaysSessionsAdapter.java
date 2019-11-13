package com.main.android.activium.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.main.android.activium.DataClasses.Sessions;
import com.main.android.activium.R;
import com.main.android.activium.VideoActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TodaysSessionsAdapter extends ArrayAdapter<Sessions> {

    private Activity context;

    public TodaysSessionsAdapter(Activity context, ArrayList<Sessions> session){
        super(context,0, session);
        // set context
        this.context = context;
        /*Log.d("TodaysSessionsAdapter","UpcomingSessionsAdapter triggered");*/
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*Log.d("TodaysSessionsAdapter","getView triggered");*/

        //TODO: Use viewHolder to improve performance

        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.list_item_session_today,
                    parent,false);
            /*Log.d("TodaysSessionsAdapter/listItem","listItem: " + listItem.toString());*/
        }

        Sessions session = getItem(position);

        // Current date and time
        Calendar calender = Calendar.getInstance();
        Date currDate = calender.getTime();

        // assign variables
        String consultantImgUrl = null;
        String consultantName = null;
        Boolean isNutritionist = false;
        Date dateTime = null;
        String roomName = null;
        Intent intent = new Intent(context, VideoActivity.class);

        // link view
        ImageView consultantPic = listItem.findViewById(R.id.consultant_pic_todays_sess);
        TextView consultantNameTextView = listItem.findViewById(R.id.consultantName);
        TextView consultanttypeTextView = listItem.findViewById(R.id.consultantType);
        TextView countDownTextView = listItem.findViewById(R.id.count_down);
        TextView startingTimeTextView = listItem.findViewById(R.id.starting_time);
        LinearLayout countDownLayout = listItem.findViewById(R.id.countDown_Layout);
        LinearLayout happeningLaterLayout = listItem.findViewById(R.id.happeningLater_Layout);
        Button enterSessionBtn = listItem.findViewById(R.id.enter_todaysSess);

        try {
            consultantName = session.getConsultantName();
            /*Log.d("TodaysSessionsAdapter","consultantName: " + consultantName);*/
            consultantImgUrl = session.getConsultantImgUrl();
            /*Log.d("TodaysSessionsAdapter","consultantImgUrl: " + consultantImgUrl);*/
            dateTime = session.getDateTiem();
            /*Log.d("TodaysSessionsAdapter","dateTime: " + dateTime);*/
            roomName = session.getRoomName();
            /*Log.d("TodaysSessionsAdapter","roomName: " + roomName);*/

            // TODO: HANDLE STATES
            Boolean approved = session.getApproved();
            /*Log.d("TodaysSessionsAdapter","approved: " + approved);*/
            Boolean completed = session.getCompleted();
            /*Log.d("TodaysSessionsAdapter","completed: " + completed);*/
            Boolean missed = session.getMissed();
            /*Log.d("TodaysSessionsAdapter","missed: " + missed);*/
            Boolean cancelled = session.getCancelled();
            /*Log.d("TodaysSessionsAdapter","cancelled: " + cancelled);*/

            isNutritionist =session.getIsNutritionist();
            /*Log.d("TodaysSessionsAdapter","isNutritionist: " + isNutritionist);*/
        }catch(Exception e){
            Log.e("TodaysSessionsAdapter", "error returning UpcomingSessionsAdapter values");
        }

        //set final value for roomNAme
        final String roomNameFinal = roomName;
        // 15 minutes after session time
        Calendar calender_plus15Muin = Calendar.getInstance();
        calender_plus15Muin.setTime(dateTime);
        calender_plus15Muin.add(Calendar.MINUTE, 15);
        Date sessions_Plus15Min = calender_plus15Muin.getTime();
        // 20 minutes before session time
        Calendar calender_minus20Muin = Calendar.getInstance();
        calender_minus20Muin.setTime(dateTime);
        calender_minus20Muin.add(Calendar.MINUTE, -20);
        Date sessions_Minus20Min = calender_minus20Muin.getTime();

        // set Consultant photo
        if (consultantImgUrl != null){
            // set drawer profile image
            Glide.with(listItem).load(consultantImgUrl).apply(new RequestOptions()
                    .override(150, 150)).apply(RequestOptions.circleCropTransform())
                    .into(consultantPic);
        }else {
            Log.e("TodaysSessionsAdapter", "Error consultant photo url is null");
        }

        // set consultant name
        if (consultantName != null){
            consultantNameTextView.setText(consultantName);
        }else{
            Log.e("TodaysSessionsAdapter", "error consultant name is null");
        }

        // set consultant type
        if (isNutritionist){
            consultanttypeTextView.setText(R.string.nutritionist);
        }else{
            consultanttypeTextView.setText("");
            Log.e("TodaysSessionsAdapter", "error consultant type wasn't found");
        }

        /*Log.d("TodaysSessionsAdapter",
                "condition 1, dateTime: " + dateTime
                + " currDate: " + currDate
                + " sessions_Minus20Min: " + sessions_Minus20Min);*/
        // Set count down if session is in the future
        if (dateTime != null && currDate.compareTo(dateTime) < 0
                && currDate.compareTo(sessions_Minus20Min) > 0) {
            // Get time left to session in milliseconds
            long msToSession = dateTime.getTime() - currDate.getTime();
            // show count down
            countDownLayout.setVisibility(View.VISIBLE);
            // hide enter session button
            enterSessionBtn.setVisibility(View.GONE);
            // Start count down text
            new CountDownTimer(msToSession, 1000) {
                public void onTick(long millisUntilFinished) {
                    countDownTextView.setText(millisUntilFinished / 60000 + " m");
                }
                public void onFinish() {
                    // Hide count down
                    countDownLayout.setVisibility(View.GONE);
                    // show and set enter session button
                    enterSessionBtn.setVisibility(View.VISIBLE);
                    enterSessionBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (roomNameFinal != null) {
                                /*context.finish();*/
                                intent.putExtra("roomName", roomNameFinal);
                                context.startActivity(intent);
                            }else{
                                Log.e("TodaysSessionsAdapter", "Error roomName is null");
                                Toast.makeText(context, "Session not found!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }.start();
        }
        // set button visible if within 15 mins before or after session time
        else if (dateTime != null && currDate.compareTo(dateTime) >= 0 && currDate.compareTo(sessions_Plus15Min) <= 0){
            /*Log.d("TodaysSessionsAdapter", "button visibility block triggered");*/
            // Hide count down
            countDownLayout.setVisibility(View.GONE);
            // show and set enter session button
            enterSessionBtn.setVisibility(View.VISIBLE);
            enterSessionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (roomNameFinal != null) {
                        /*context.finish();*/
                        intent.putExtra("roomName", roomNameFinal);
                        context.startActivity(intent);
                    }else{
                        Log.e("TodaysSessionsAdapter", "Error roomName is null");
                        Toast.makeText(context, "Session not found!",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        // set button visible if within 15 mins before or after session time
        else {
            /*Log.d("TodaysSessionsAdapter", "Happedning later triggered ");*/
            Calendar sessionTimeCalendar = Calendar.getInstance();
            sessionTimeCalendar.setTime(dateTime);
            String hours = String.valueOf(sessionTimeCalendar.get(Calendar.HOUR));
            String minutes = String.valueOf(sessionTimeCalendar.get(Calendar.MINUTE));
            String amPm = null;
            int amPmInt = sessionTimeCalendar.get(Calendar.AM_PM);
            if (amPmInt == 0){
                amPm = "AM";
            }else if (amPmInt == 1){
                amPm = "PM";
            }
            String hoursMinutesamPm = hours + ":" + minutes + " "  + amPm;
            startingTimeTextView.setText(hoursMinutesamPm);
            happeningLaterLayout.setVisibility(View.VISIBLE);
        }

        return listItem;
    }
}
