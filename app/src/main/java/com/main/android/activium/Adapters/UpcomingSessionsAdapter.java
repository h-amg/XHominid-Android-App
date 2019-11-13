package com.main.android.activium.Adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.main.android.activium.DataClasses.Sessions;
import com.main.android.activium.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpcomingSessionsAdapter extends ArrayAdapter<Sessions> {

    private Activity context;
    // Schedule sessions interface
    private SechduleSessInterface scheduleSessionsListener;

    public UpcomingSessionsAdapter(Activity context, ArrayList<Sessions> session){
        super(context,0, session);
        // set context
        this.context = context;
        /*Log.d("UpcomingSessionsAdapter","UpcomingSessionsAdapter triggered");*/
        try {
            scheduleSessionsListener = (SechduleSessInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement SechduleSessInterface");
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*Log.d("UpcomingSessionsAdapter","getView triggered");*/

        //TODO: Use viewHolder to improve performance

        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.list_item_session_upcoming,
                    parent,false);
            /*Log.d("UpcomingSessionsAdapter/listItem","listItem: " + listItem.toString());*/
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
        Boolean approved = false;
        Boolean scheduled = false;
        String sessionId = null;
        Boolean beingScheduled = false;
        Log.d("UpcomingSessionsAdapter", "beingScheduled value: " + beingScheduled);

        // link view
        ImageView consultantPic = listItem.findViewById(R.id.consultant_picc_upcoming);
        TextView consultantNameTextView = listItem.findViewById(R.id.consultantName_upcoming);
        TextView consultanttypeTextView = listItem.findViewById(R.id.consultantType_upcoming);
        TextView dateTextView = listItem.findViewById(R.id.date_time_sess);
        TextView approvalStatus = listItem.findViewById(R.id.approval_status);
        Button scheduleSessionBtn = listItem.findViewById(R.id.schedule_upcoming_ses);
        SpinKitView schedulinganimation = listItem.findViewById(R.id.upcoming_sess_beingScheduled);
        LinearLayout dateAndAproval_Layout = listItem.findViewById(R.id.upcominSess_dateAproval_Layout);

        try {
            consultantName = session.getConsultantName();
            /*Log.d("UpcomingSessionsAdapter","consultantName: " + consultantName);*/
            consultantImgUrl = session.getConsultantImgUrl();
            /*Log.d("UpcomingSessionsAdapter","consultantImgUrl: " + consultantImgUrl);*/
            dateTime = session.getDateTiem();
            /*Log.d("UpcomingSessionsAdapter","dateTime: " + dateTime);*/
            sessionId = session.getSessionId();
            //Log.d("UpcomingSessionsAdapter","sessionId: " + sessionId);
            beingScheduled = session.getBeingScheduled();
            //Log.d("UpcomingSessionsAdapter","beingScheduled: " + beingScheduled);

            // TODO: HANDLE STATES
            approved = session.getApproved();
            /*Log.d("UpcomingSessionsAdapter","approved: " + approved);*/
            Boolean completed = session.getCompleted();
            /*Log.d("UpcomingSessionsAdapter","completed: " + completed);*/
            Boolean missed = session.getMissed();
            /*Log.d("UpcomingSessionsAdapter","missed: " + missed);*/
            Boolean cancelled = session.getCancelled();
            /*Log.d("UpcomingSessionsAdapter","cancelled: " + cancelled);*/
            scheduled = session.getScheduled();
            /*Log.d("UpcomingSessionsAdapter","scheduled: " + scheduled);*/

            isNutritionist =session.getIsNutritionist();
            /*Log.d("UpcomingSessionsAdapter","isNutritionist: " + isNutritionist);*/
        }catch(Exception e){
            Log.e("UpcomingSessionsAdapter", "error returning UpcomingSessionsAdapter values");
        }

        // set Consultant photo
        if (consultantImgUrl != null){
            // set drawer profile image
            Glide.with(listItem).load(consultantImgUrl).apply(new RequestOptions()
                    .override(150, 150)).apply(RequestOptions.circleCropTransform())
                    .into(consultantPic);
        }else {
            Log.e("UpcomingSessionsAdapter", "Error consultant photo url is null");
        }

        // set consultant name
        if (consultantName != null){
            consultantNameTextView.setText(consultantName);
        }else{
            Log.e("UpcomingSessionsAdapter", "error consultant name is null");
        }

        // set consultant type
        if (isNutritionist){

            consultanttypeTextView.setText(R.string.nutritionist);
        }else{
            consultanttypeTextView.setText("");
            Log.e("UpcomingSessionsAdapter", "error consultant type wasn't found");
        }

        if (beingScheduled){
            Log.d("UpcomingSessionsAdapter", "beingScheduled value2 triggered: " + beingScheduled);
            scheduleSessionBtn.setVisibility(View.GONE);
            dateAndAproval_Layout.setVisibility(View.GONE);
            schedulinganimation.setVisibility(View.VISIBLE);
        }



        // handle approved and scheduled state
        if (approved && scheduled) {
            // set session scheduled date
            SimpleDateFormat Dateformatter = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
            SimpleDateFormat Dayformatter = new SimpleDateFormat("EE", Locale.getDefault());
            String dateString = Dayformatter.format(dateTime) + " " + Dateformatter.format(dateTime);
            dateTextView.setText(dateString);
            // Set approval status
            approvalStatus.setText(R.string.approved);
            approvalStatus.setTextColor(ContextCompat.getColor(context, R.color.acceptGreen));

            schedulinganimation.setVisibility(View.GONE);
            scheduleSessionBtn.setVisibility(View.GONE);
            dateAndAproval_Layout.setVisibility(View.VISIBLE);
        // handle non_approved and scheduled state
        }else if (scheduled){
            // set session scheduled date
            SimpleDateFormat Dateformatter = new SimpleDateFormat("dd.MM HH:mm a", Locale.getDefault());
            SimpleDateFormat Dayformatter = new SimpleDateFormat("EE", Locale.getDefault());
            String dateString = Dayformatter.format(dateTime) + " " + Dateformatter.format(dateTime);
            dateTextView.setText(dateString);
            // set approval status to pending
            approvalStatus.setText(R.string.pending);
            approvalStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

            scheduleSessionBtn.setVisibility(View.GONE);
            schedulinganimation.setVisibility(View.GONE);
            dateAndAproval_Layout.setVisibility(View.VISIBLE);
        // handle non_approved and non_scheduled state
        }else if (!beingScheduled){
            final String finalSessionID = sessionId;
            scheduleSessionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scheduleSessionsListener.scheduleSession(finalSessionID, position);
                }
            });
            schedulinganimation.setVisibility(View.GONE);
            dateAndAproval_Layout.setVisibility(View.GONE);
            scheduleSessionBtn.setVisibility(View.VISIBLE);
        }
        return listItem;
    }

    public interface SechduleSessInterface {
        void scheduleSession(String sessionId, int position);
    }
}
