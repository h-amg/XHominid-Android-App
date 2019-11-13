package com.main.android.activium.DataClasses;

import android.util.Log;

import com.main.android.activium.util.DailyMacChart;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsData {

    // TODO: DECLARE VARIABLES
    private int avgTarCal = 0;
    private int avgTarProt = 0;
    private int avgTarCarbs = 0;
    private int avgTarFat = 0;
    private Document mDiet;
    private ArrayList<DailyMacChart> chartMacros ;

    // TODO: SETUP AN OBSERVER IN STATS ACTIVITY

    public StatsData(Document diet, List<Document> dailyMacDocs, Date fromDate, Date toDate){

        /*Log.d("StatsData", "fromdate: " + fromDate + "toDate: " + toDate);*/

        int totalCount = 0;
        double onTarCal   = 0.0;
        double onTarProt  = 0.0;
        double onTarCarbs = 0.0;
        double onTarfat   = 0.0;

        try {
            Diet userDiet = new Diet(diet);
            totalCount = dailyMacDocs.size();
            chartMacros = new ArrayList<DailyMacChart>();

            // diet variables
            Double dietCal = userDiet.getCal();
            Double dietProt = userDiet.getProt();
            Double dietCarbs = userDiet.getFat();
            Double dietFat = userDiet.getCarbs();

            /*Log.d("StatsData",String.format("Size of dailyMacDocs: %d", dailyMacDocs.size()));*/
            for (Document doc : dailyMacDocs) {
                DailyMac userDailyMAc = new DailyMac(doc);
                Double dailyMacCal = userDailyMAc.getCal();
                Double dailyMacProt = userDailyMAc.getProt();
                Double dailyMacCarbs = userDailyMAc.getFat();
                Double dailyMacFat = userDailyMAc.getCarbs();

                Date macDate = userDailyMAc.getTime();
                /*Log.d("StatsData", "macDate: " + macDate);*/
                /*Log.d("StatsData", "toDate: " + toDate);*/
                /*Log.d("StatsData", "fromDate: " + fromDate);*/

                // check if date of daily macros document is between from and to date selected by the user
                if (macDate != null) {
                    if (macDate.compareTo(toDate) <= 0 && macDate.compareTo(fromDate) >= 0) {
                        chartMacros.add(new DailyMacChart(userDailyMAc, userDiet));
                        /*Log.d("StatsData",String.format("No. of chart macros doc added: %d", chartMacros.size()));*/
                    }
                }

                if ((dailyMacCal) >= (dietCal * 0.85) && (dailyMacCal) <= (dietCal * 1.15)) {
                    onTarCal += 1;
                }
                if ((dailyMacProt) >= (dietProt * 0.85) && (dailyMacProt) <= (dietProt * 1.15)) {
                    onTarProt += 1;
                }
                if ((dailyMacCarbs) >= (dietCarbs * 0.85) && (dailyMacCarbs) <= (dietCarbs * 1.15)) {
                    onTarCarbs += 1;
                }
                if ((dailyMacFat) >= (dietFat * 0.85) && (dailyMacFat) <= (dietFat * 1.15)) {
                    onTarfat += 1;
                }
            }
            avgTarCal = (int) Math.round((onTarCal / totalCount) * 100);
            /*Log.d("StatsData", "avgTarCal: " + avgTarCal +  " "  + "onTarCal: "
                    + onTarCal + " " + "totalCount: " + totalCount);*/

            avgTarProt = (int) Math.round((onTarProt / totalCount) * 100);
            /*Log.d("StatsData", "avgTarProt: " + avgTarProt +  " "  + "onTarProt: "
                    + onTarProt + " " + "totalCount: " + totalCount);*/

            avgTarCarbs = (int) Math.round((onTarCarbs / totalCount) * 100);
            /*Log.d("StatsData", "avgTarCarbs: " + avgTarCarbs +  " "  + "onTarCarbs: "
                    + onTarCarbs + " " + "totalCount: " + totalCount);*/

            avgTarFat = (int) Math.round((onTarfat / totalCount) * 100);
            /*Log.d("StatsData", "avgTarFat: " + avgTarFat +  " "  + "onTarfat: "
                    + onTarfat + " " + "totalCount: " + totalCount);*/
        }catch (Exception e){
            Log.e("StatsData", "Error retrieving data from StatsData document");
        }
    }

    public int getavgTarCal(){
        return avgTarCal;
    }

    public int getavgTarProt(){
        return avgTarProt;
    }

    public int getAvgTarCarbs(){
        return avgTarCarbs;
    }

    public int getAvgTarFat(){
        return avgTarFat;
    }

    public ArrayList<DailyMacChart> getChartMacros(){
        return chartMacros;
    }
}
