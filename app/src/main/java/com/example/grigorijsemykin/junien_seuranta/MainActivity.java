package com.example.grigorijsemykin.junien_seuranta;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

//    public final String TEST_URL = "https://rata.digitraffic.fi/api/v1/live-trains?arrived_trains=0&arriving_trains=15&departed_trains=0&departing_trains=15&station=TKL";

    JSON_Parser jp = new JSON_Parser(this);

    public static HashMap<String, String> STATIONCODES;
    public static HashMap<String, String> STATIONS;
    public static ArrayList<String> stationNames;

    TableLayout table;
    MultiAutoCompleteTextView arrivalStationInput, departureStationInput;
    String arrScode, depScode, startTime, endTime;
    AppDatabase db;
    ProgressBar progressBar;

    private boolean live = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        table = (TableLayout) findViewById(R.id.timeTable);
        db = AppDatabase.getAppDatabase(this);

        Switch liveSwitch = (Switch) findViewById(R.id.liveSwitch);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        liveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    live = true;
                } else {
                    live = false;
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


        TextClock textClock = (TextClock) findViewById(R.id.textClock);
        textClock.setFormat12Hour(null);
        textClock.setFormat24Hour("HH:mm:ss");

        STATIONCODES = new HashMap<String, String>();
        stationNames = new ArrayList<String>();

        jp.getStationNames();

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, stationNames);
        arrivalStationInput = (MultiAutoCompleteTextView)findViewById(R.id.arrivalStationInput);
        arrivalStationInput.setAdapter(adapter1);
        arrivalStationInput.setTokenizer(new SpaceTokenizer());

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, stationNames);
        departureStationInput = (MultiAutoCompleteTextView)findViewById(R.id.departureStationInput);
        departureStationInput.setAdapter(adapter2);
        departureStationInput.setTokenizer(new SpaceTokenizer());
    }


    public void onHaeClick(View view) {
        if (departureStationInput.getText().toString().equals(null) || departureStationInput.getText().toString().equals("")) {
            Toast.makeText(this, "Lähtöasema on tyhjä!", Toast.LENGTH_SHORT).show();
            return;
        }

        depScode = STATIONCODES.get(departureStationInput.getText().toString());
//        arrScode = STATIONCODES.get(arrivalStationInput.getText().toString());

        if (isNetworkAvailable()){
            try {
                if (live) {
                    progressBar.setVisibility(View.VISIBLE);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                        try {
                            while(live) {
                                System.out.println("UPDATING 5 sec. delay");
                                new JsonTask().execute("https://rata.digitraffic.fi/api/v1/live-trains?arrived_trains=0&arriving_trains=15&departed_trains=0&departing_trains=15&station="+depScode);
//                                new JsonTask().execute("https://rata.digitraffic.fi/api/v1/schedules?departure_station=" + depScode + "&arrival_station=" + arrScode + "&from=" + getStartTime() + ".000Z&to=" + getEndTime() + ".000Z&limit=15");
                                sleep(5000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        }
                    };

                    thread.start();
                } else {
                    new JsonTask().execute("https://rata.digitraffic.fi/api/v1/live-trains?arrived_trains=0&arriving_trains=15&departed_trains=0&departing_trains=15&station="+depScode);
//                    new JsonTask().execute("https://rata.digitraffic.fi/api/v1/schedules?departure_station=" + depScode + "&arrival_station=" + arrScode + "&from=" + getStartTime() + ".000Z&to=" + getEndTime() + ".000Z&limit=15");
                }
            } catch (Exception e) {
                Toast.makeText(this, "Reading the web page is unsuccessful", Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(this, "Network is not available", Toast.LENGTH_SHORT).show();
    }

//    private String getStartTime() {
//        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime()).replaceAll("_", "T");
//    }
//    private String getEndTime() {
//        DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MINUTE, 30);
//        return timeFormat.format(cal.getTime()).replaceAll("_", "T");
//    }



    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected()) ? true : false;
    }


    public void onAddToFavoritesClick() {

        String arrStation = arrivalStationInput.getText().toString();
        String depStation = departureStationInput.getText().toString();

        if (!arrStation.equals("") && !depStation.equals("")) {

            Route route = new Route();
            route.setArrStation(arrivalStationInput.getText().toString());
            route.setDepStation(departureStationInput.getText().toString());
            db.routeDao().insertAll(route);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateTimeTable(ArrayList<Juna> trainsData) {

        clearTimeTable();

        for (Juna j : trainsData) {

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    for (int i=0; i<table.getChildCount(); i++) {
                        table.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                    v.setBackgroundColor(Color.CYAN);
                }
            });

            for (int i=0; i<4; i++) {

//                if (i==0) {
//                    ImageView view = new ImageView(this);
//                    view.setImageResource(android.R.drawable.ic_menu_search);
//                    view.setLayoutParams(new TableRow.LayoutParams(i));
//                    view.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
//                    view.setBackground(getResources().getDrawable(R.drawable.borderbottom));
//                    row.addView(view);
//                } else {

                    TextView tv = new TextView(this);
                    tv.setLayoutParams(new TableRow.LayoutParams(i));
                    tv.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
                    tv.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    tv.setBackground(getResources().getDrawable(R.drawable.borderbottom));
                    tv.setPadding(0, 5, 0, 5);
                    tv.setTextSize(20);

                    switch (i) {
                        case 0:
//                            tv.setText(j.getDepTime().substring(11, 19));
                            tv.setText(j.getDepTime());
                            break;
                        case 1:
                            tv.setText(j.getTrainLetter());
                            break;
                        case 2:
                            tv.setText(j.getTrack());
                            break;
                        case 3:
                            tv.setText(j.getDestination());
                            break;
                    }

                    row.addView(tv);
//                }
            }

            table.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        }
    }


    private void clearTimeTable() {
        int tableRows = table.getChildCount();
        for (int i = 1; i < tableRows; i++) {
            table.removeView(table.getChildAt(1));
        }
    }



    //-------------LOAD JSON FROM URL-------------------

    private class JsonTask extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                System.out.println(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                String line = "";
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            updateTimeTable(jp.getData2(result, arrScode, depScode));
        }
    }


}