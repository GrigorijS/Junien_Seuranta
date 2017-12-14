package com.example.grigorijsemykin.junien_seuranta;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Grigorij Semykin on 4.12.2017.
 */

public class JSON_Parser {

    Context context;
    Juna juna = new Juna();

    public JSON_Parser(Context context) {
        this.context = context;
    }

    public void getStationNames() {
        try {
            InputStream is = this.context.getResources().openRawResource(R.raw.station_names);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject jObject = new JSONObject(json);
            JSONArray m_jArry = jObject.getJSONArray("stations");

            String stationShortCode = "";
            String stationName = "";

            for (int i = 0; i < m_jArry.length(); i++) {
                stationShortCode = m_jArry.getJSONObject(i).getString("stationShortCode");
                stationName = m_jArry.getJSONObject(i).getString("stationName");

                MainActivity.STATIONCODES.put(stationName, stationShortCode);
//                MainActivity.STATIONS.put(stationShortCode, stationName);
                MainActivity.stationNames.add(stationName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ArrayList<Juna> getData2(String data, String arrStation, String depStation) {
        ArrayList<Juna> junat = new ArrayList<>();

        boolean skip = false;

        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Calendar cal = Calendar.getInstance();
        String time = timeFormat.format(cal.getTime());

        Calendar time1 = Calendar.getInstance();
        Calendar time2 = Calendar.getInstance();

        String leavingTime = "", trainTrack = "", trainID = "", destination = "";

        try {

            JSONArray mainArray = new JSONArray(data);

            for (int i = 0; i < mainArray.length(); i++) {

                JSONObject jsonObject = (JSONObject) mainArray.get(i);

                trainID = jsonObject.get("commuterLineID").toString();

                if (trainID.equals("") || trainID == null) {
                    trainID = mainArray.getJSONObject(i).getString("trainType")+" "+mainArray.getJSONObject(i).getString("trainNumber");
                }

                JSONArray stops = (JSONArray) jsonObject.get("timeTableRows");

                for (int j = 0; j < stops.length(); j++) {
                    skip = false;
                    JSONObject stopsObj = (JSONObject) stops.get(j);
                    JSONObject lastObj = (JSONObject) stops.get(stops.length()-1);

                    destination = lastObj.get("stationShortCode").toString();

                    if (stopsObj.get("stationShortCode").equals(depStation)) {
                        if (stopsObj.get("type").equals("DEPARTURE")) {
                            trainTrack = stopsObj.get("commercialTrack").toString();
                            leavingTime = formatDate(stopsObj.get("scheduledTime").toString());

//                            System.out.println("TIME: "+leavingTime+ "    TRACK: "+trainTrack+"    TRAIN: "+trainID);

                            time1.setTime(timeFormat.parse(leavingTime));
                            time2.setTime(timeFormat.parse(time));

                            if (time1.before(time2) || leavingTime == null || leavingTime.equals("") || leavingTime.equals(null)) {
                                skip = true;
                            }
                            break;

                        }
                    }
                }

                if (!skip) {
                    juna = new Juna(leavingTime, trainTrack, trainID, destination);
                    junat.add(juna);
                }

            }

            Collections.sort(junat, new Comparator<Juna>() {
                public int compare(Juna o1, Juna o2) {
                    return o1.getDepTime().compareTo(o2.getDepTime());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return junat;
    }

    private String formatDate(String strDate) {
        String newDate = "";

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.set(Integer.parseInt(strDate.substring(0, 4)),
                Integer.parseInt(strDate.substring(5, 7))-1,
                Integer.parseInt(strDate.substring(8, 10)),
                Integer.parseInt(strDate.substring(11, 13)),
                Integer.parseInt(strDate.substring(14, 16)));

        cal.add(Calendar.HOUR_OF_DAY, 2); // adds one hour

//        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        newDate = sdf.format(cal.getTime());

        return newDate;
    }



}