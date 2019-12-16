package com.example.ttn_compass2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.support.design.widget.BottomNavigationView;

public class Main2Activity extends FragmentActivity {

    Location mlocation;
    FusedLocationProviderClient mFusedLocationClient;
    private static final  int Request_code = 101;

    // device sensor manager
    private SensorManager mSensorManager;
    private LocationManager locationManager;

    ArrayList<HashMap<String, String>> location = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;

    ListView listView;
    TextView textView;
    TextView txtview;
    TextView txtview2;
    String[] listItem;
    Spinner spin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        locationManager =  (LocationManager) getSystemService(LOCATION_SERVICE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        GetLastLocation();


        String[] users = { "Italy", "Spain", "France", "US", "Ethiopia" };

        spin = (Spinner) findViewById(R.id.inputSearch);
        Button btnShow = (Button) findViewById(R.id.buttonSearch);
        txtview = (TextView) findViewById((R.id.textview1));
        txtview2 = (TextView) findViewById((R.id.textview2));

        ArrayAdapter<String> adapterx = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, users);
        adapterx.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapterx);

        listView=(ListView)findViewById(R.id.listview1);

        btnShow.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                double lat1 = mlocation.getLatitude();
                double lon1 =  mlocation.getLongitude();


                //double lat1 = 45.7034685;
                //double lon1 = 13.7187782;

                String unit = "k";

                String country = spin.getSelectedItem().toString();

                txtview.setText("Number of TTN gateway get from" + country);

                try{

                    JSONObject obj = new JSONObject(loadJSONFromAsset(country));

                    JSONObject obj2 = (JSONObject)obj.get("ttn_node");
                    Iterator iterator = obj2.keys();
                    String key = null;
                    int num_node = 0;

                    ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
                    HashMap<String, String> m_li;

                    while (iterator.hasNext()){
                        num_node = num_node + 1;
                        key = (String)iterator.next();

                        String id = ""+((JSONObject)obj2.get(key)).get("id");
                        //String description = ""+((JSONObject)obj2.get(key)).get("description");
                        String location = ""+((JSONObject)obj2.get(key)).get("location");


                        //String lat = location.substring(13);
                        String lat = location;
                        String lon = location;

                        String str = location;
                        Pattern patternLat = Pattern.compile("\"latitude\":(.*?),", Pattern.DOTALL);
                        Matcher matcherLat = patternLat.matcher(str);
                        while (matcherLat.find()) {
                            lat = matcherLat.group(1);
                        }

                        Pattern patternLon = Pattern.compile("\"longitude\":(.*?),", Pattern.DOTALL);
                        Matcher matcherLon = patternLon.matcher(str);
                        while (matcherLon.find()) {
                            lon = matcherLon.group(1);
                        }
                        String loc = lat +","+ lon;

                        double lat2 = Double.parseDouble(lat);
                        double lon2 = Double.parseDouble(lon);


                        double d = distance(lat1, lon1, lat2, lon2, unit);
                        String dis = Double.toString(d) +" km";

                        //Add your values in your 'ArrayList' as below:
                        m_li = new HashMap<String, String>();
                        m_li.put("id", id);
                        m_li.put("location", loc);
                        m_li.put("distance", dis);



                        formList.add(m_li);

                        ListAdapter adapter = new SimpleAdapter(Main2Activity.this, formList,
                                R.layout.list_item, new String[]{ "id","location", "distance"},
                                new int[]{R.id.id, R.id.location, R.id.distance});
                        listView.setAdapter(adapter);
                    }
                    txtview.append(" are " + Integer.toString(num_node));


                    JSONArray m_jArry = obj2.getJSONArray("ttn_node");

                    /* */
                } catch (JSONException e) {
                    e.printStackTrace();
                    //Toast.makeText(getApplicationContext(), "fail 2", Toast.LENGTH_LONG).show();

                }

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(view.getContext(), MapsActivity.class);
                    startActivityForResult(myIntent, 0);
                }

                if (position == 1) {
                    Intent myIntent = new Intent(view.getContext(), MapsActivity.class);
                    startActivityForResult(myIntent, 0);
                }

                if (position == 2) {
                    Intent myIntent = new Intent(view.getContext(), MapsActivity.class);
                    startActivityForResult(myIntent, 0);
                }

                if (position == 3) {
                    Intent myIntent = new Intent(view.getContext(), MapsActivity.class);
                    startActivityForResult(myIntent, 0);
                }

                if (position == 4) {
                    Intent myIntent = new Intent(view.getContext(), MapsActivity.class);
                    startActivityForResult(myIntent, 0);
                }



            }
        });



    }

    public void showGatewayList(){

    }



    public String loadJSONFromAsset(String country) {
        String json = null;


        try {
            if(country.equals("Italy")) {
                final InputStream is = getApplicationContext().getAssets().open("ttn_italy.json");

                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

                JSONObject obj = new JSONObject(json);
            }
            else if(country.equals("Spain")){
                final InputStream is = getApplicationContext().getAssets().open("ttn_spain.json");

                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

                JSONObject obj = new JSONObject(json);

            }

        } catch (final Throwable ex) {
            ex.printStackTrace();
            return null;


        }

        return json;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {

            double R = 6371;

            double φ1 = Math.toRadians(lat1);
            double φ2 = Math.toRadians(lat2);
            double Δφ = Math.toRadians(lat2-lat1);
            double Δλ = Math.toRadians(lon2-lon1);

            double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                    Math.cos(φ1) * Math.cos(φ2) *
                            Math.sin(Δλ/2) * Math.sin(Δλ/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

            double dist = R * c;

            /*
            double theta = lon1 - lon2;

            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit == "K") {
                dist = dist * 1.609344;
            } else if (unit == "N") {
                dist = dist * 0.8684;
            }

            // Haversine formula
            */
            DecimalFormat df2 = new DecimalFormat("###.##");
            return (Double.valueOf(df2.format(dist)));
        }
    }

    private void GetLastLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_code);
            return;
        }
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location !=null){
                    mlocation = location;
                    Toast.makeText(getApplicationContext(),mlocation.getLatitude()+ "," +mlocation.getLongitude(),
                            Toast.LENGTH_SHORT).show();
                    txtview2.append(mlocation.getLatitude()+","+mlocation.getLongitude());
                }
            }
        });
    }



    public void stop() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //start();
    }
}
