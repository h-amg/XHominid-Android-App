package com.main.android.activium.util;

import com.main.android.activium.DataClasses.DailyMac;
import com.main.android.activium.DataClasses.Diet;

import java.util.Date;

public class DailyMacChart {

    //initiate variables
    private Date xVal;
    private int  yVal;


    public DailyMacChart(DailyMac dailyMacDoc, Diet dietDoc){
        xVal = dailyMacDoc.getTime();
        yVal = (int) Math.round((dailyMacDoc.getCal()/dietDoc.getCal())*100);
    }

    public Date getxVal(){
        return xVal;
    }

    public  int getyVal(){
        return yVal;
    }
}
