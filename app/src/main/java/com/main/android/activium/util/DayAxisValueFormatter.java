package com.main.android.activium.util;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by philipp on 02/06/16.
 */
public class DayAxisValueFormatter extends ValueFormatter {


   /* private final String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    private final BarLineChartBase<?> chart;*/
   /* public DayAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }*/
    private ArrayList<DailyMacChart> mMacros;



    public DayAxisValueFormatter(ArrayList<DailyMacChart> macros) {
        mMacros = macros;
    }

    @Override
    public String getFormattedValue(float value) {

        int idx = (int) value;
        if(idx < mMacros.size()){
            return stringOfDate(mMacros.get(idx).getxVal());
        }else{
            return "";
        }


        // pld implementations
        /*String dayname = "";
        int day = (int) value;

        switch(day){
            case 0:
                dayname = "Sun";
                break;
            case 1:
                dayname = "Mon";
                break;
            case 2:
                dayname = "Tue";
                break;
            case 3:
                dayname = "Wed";
                break;
            case 4:
                dayname = "Thu";
                break;
            case 5:
                dayname = "Fri";
                break;
            case 6:
                dayname = "Sat";
        }

        return dayname;*/
    }

    private String stringOfDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM");
        String currDateString = formatter.format(date);
        SimpleDateFormat DayNameFrmt = new SimpleDateFormat("EE");
        String fullDateString = DayNameFrmt.format(date) + " " + currDateString;
        /*Log.d("DayXformat", "stringOfDate: " + fullDateString);*/
        return fullDateString;
    }

    private int getDaysForMonth(int month, int year) {

        // month is 0-based

        if (month == 1) {
            boolean is29Feb = false;

            if (year < 1582)
                is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
            else if (year > 1582)
                is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);

            return is29Feb ? 29 : 28;
        }

        if (month == 3 || month == 5 || month == 8 || month == 10)
            return 30;
        else
            return 31;
    }

    private int determineMonth(int dayOfYear) {

        int month = -1;
        int days = 0;

        while (days < dayOfYear) {
            month = month + 1;

            if (month >= 12)
                month = 0;

            int year = determineYear(days);
            days += getDaysForMonth(month, year);
        }

        return Math.max(month, 0);
    }

    private int determineDayOfMonth(int days, int month) {

        int count = 0;
        int daysForMonths = 0;

        while (count < month) {

            int year = determineYear(daysForMonths);
            daysForMonths += getDaysForMonth(count % 12, year);
            count++;
        }

        return days - daysForMonths;
    }

    private int determineYear(int days) {

        if (days <= 366)
            return 2016;
        else if (days <= 730)
            return 2017;
        else if (days <= 1094)
            return 2018;
        else if (days <= 1458)
            return 2019;
        else
            return 2020;

    }
}
