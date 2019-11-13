package com.main.android.activium.util;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class WeekBoundaries {


    private Date mWeekStart;
    private Date mWeekEnd;
    private Date mNextWeekStart;
    private Date mNextWeekEnd;

    public WeekBoundaries(){
        //Setup calendar
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.set(Calendar.HOUR_OF_DAY, 0);
        calendarNow.clear(Calendar.MINUTE);
        calendarNow.clear(Calendar.SECOND);
        calendarNow.clear(Calendar.MILLISECOND);
        // get start of this week in milliseconds
        calendarNow.set(Calendar.DAY_OF_WEEK, calendarNow.getFirstDayOfWeek());
        mWeekStart = calendarNow.getTime();
        //Log.d("WeekBoundaries", "This week start: " + mWeekStart);
        // get end of this week in milliseconds
        Calendar calenderThisWeekEnd = calendarNow;
        calenderThisWeekEnd.add(Calendar.DAY_OF_WEEK, 8);// start of the week at hour 00 and the end before hour 00 of the beginning of the week after
        mWeekEnd = calenderThisWeekEnd.getTime();
        //Log.d("WeekBoundaries", "This week end: " + mWeekEnd);
        // Setup calendar
        calendarNow.add(Calendar.WEEK_OF_YEAR, 1);
        // get start of next week in milliseconds
        calendarNow.set(Calendar.DAY_OF_WEEK, calendarNow.getFirstDayOfWeek());
        mNextWeekStart = calendarNow.getTime();
        //Log.d("WeekBoundaries", "Next week start: " + mNextWeekStart);
        // get end of next week in milliseconds
        Calendar calenderNextWeekEnd = calendarNow;
        calenderNextWeekEnd.add(Calendar.DAY_OF_WEEK, 8);
        mNextWeekEnd = calenderNextWeekEnd.getTime();
        //Log.d("WeekBoundaries", "Next week end: " + mNextWeekEnd);
    }

    public Date getmWeekStart(){
        return mWeekStart;
    }

    public Date getmWeekEnd(){
        return mWeekEnd;
    }

    public Date getmNextWeekStart(){
        return mNextWeekStart;
    }

    public Date getmNextWeekEnd(){
        return mNextWeekEnd;
    }
}
