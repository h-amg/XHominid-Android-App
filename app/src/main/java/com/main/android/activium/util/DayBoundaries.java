package com.main.android.activium.util;

import android.icu.util.TimeZone;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

public class DayBoundaries {


    private Date mDayStart;
    private Date mDayEnd;

    public DayBoundaries(Date day){
        Calendar c = Calendar.getInstance();
        c.setTime(day);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        mDayStart = c.getTime();  // current time in utc
        /*Log.d("DayBoundaries", "day start: " + mDayStart);*/
        // add a add a day to indicate end of the day
        c.setTime(mDayStart);
        c.add(Calendar.DAY_OF_MONTH, 1);
        mDayEnd = c.getTime(); //end of the day date
        /*Log.d("DayBoundaries", "day end: " + mDayEnd);*/

    }

    public Date getDayStart(){
        return mDayStart;
    }

    public Date getDayEnd(){
        return mDayEnd;
    }
}
