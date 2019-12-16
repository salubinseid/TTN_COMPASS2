package com.example.ttn_compass2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    Location mlocation;
    FusedLocationProviderClient mFusedLocationClient;

    private static final int Request_code = 101;

    // Latitude & Longitude
    private Double Latitude = 0.00;
    private Double Longitude = 0.00;

    ArrayList<HashMap<String, String>> gateway_location = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;


    ImageView compass_img;
    TextView txt_compass;
    int mAzimuth;

    TextView distanceView;
    // device sensor manager
    private SensorManager mSensorManager;
    private LocationManager locationManager;

    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    boolean haveSensor = false, haveSensor2 = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    ListView listView;
    TextView textView;
    String[] listItem;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //listView=(ListView)findViewById(R.id.listview1);
        //listView.getListView().setCacheColorHint(Color.rgb(36, 33, 32));
        textView = (TextView) findViewById(R.id.title);
        //textView.setTextColor(Color.BLUE);
        listItem = getResources().getStringArray(R.array.ttn_gateway);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        GetLastLocation();

        compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_compass = (TextView) findViewById(R.id.txt_compass);


        String country = "Italy";

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        Location mmlocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double lat1 = mmlocation.getLatitude();
        double lon1 =  mmlocation.getLongitude();

        String unit = "k";

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
                String location = ""+((JSONObject)obj2.get(key)).get("location");
                //String description = ""+((JSONObject)obj2.get(key)).get("description");

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
                String dis = Double.toString(d) +"KM";

                //Add your values in your 'ArrayList' as below:
                map = new HashMap<String, String>();
                map.put("LocationName", id);
                map.put("Latitude", lat);
                map.put("Longitude", lon);
                map.put("Distance", dis);

                gateway_location.add(map);


            }


        } catch (JSONException e) {
            e.printStackTrace();
        }



        //showGateway();



        start();

    }


    public void onLocationChanged(Location location) {

    }

    public void showGateway(){
        String country = "Italy";
        double lat1 = mlocation.getLatitude();
        double lon1 =  mlocation.getLongitude();

        String unit = "k";
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
                String dis = Double.toString(d) +"KM";

                //Add your values in your 'ArrayList' as below:
                map = new HashMap<String, String>();
                map.put("LocationID", id);
                map.put("Latitude", lat);
                map.put("Longitude", lon);
                map.put("LocationName", id);
                map.put("distance", dis);
                formList.add(map);

            }

            /* */
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // *** Display Google Map
        //googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        // *** Focus & Zoom
        Latitude = Double.parseDouble(gateway_location.get(0).get("Latitude").toString());
        Longitude = Double.parseDouble(gateway_location.get(0).get("Longitude").toString());
        LatLng coordinate = new LatLng(Latitude, Longitude);
        googleMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 17));


        // *** Marker (Loop)

        for (int i = 0; i < gateway_location.size(); i++) {
            Latitude = Double.parseDouble(gateway_location.get(i).get("Latitude").toString());
            Longitude = Double.parseDouble(gateway_location.get(i).get("Longitude").toString());
            String name = gateway_location.get(i).get("LocationName").toString();
            String distance = gateway_location.get(i).get("Distance").toString();

            MarkerOptions marker = new MarkerOptions().position(
                    new LatLng(Latitude, Longitude)).title(name + "\n" +
                    "Altitude (m):\n" + distance).snippet("Distance: "+distance +", ("+ Latitude + "," + Longitude + ")" );
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.gateway));
            googleMap.addMarker(marker);

        }

        // Add a marker in Trieste and move the camera
        //LatLng latLng = new LatLng(45.6560719, 13.7813442);
        LatLng latLng = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here");
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
        googleMap.addMarker(markerOptions);

    }
    public void onRequestPermissionResult(int requestCode, String [] permissions, int[] grantResult){
        switch (requestCode){
            case Request_code:
                if(grantResult.length>0 && grantResult[0] == PackageManager.PERMISSION_GRANTED){
                    GetLastLocation();
                }
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-mAzimuth);

        String where = "NW";

        if (mAzimuth >= 350 || mAzimuth <= 10)
            where = "N";
        if (mAzimuth < 350 && mAzimuth > 280)
            where = "NW";
        if (mAzimuth <= 280 && mAzimuth > 260)
            where = "W";
        if (mAzimuth <= 260 && mAzimuth > 190)
            where = "SW";
        if (mAzimuth <= 190 && mAzimuth > 170)
            where = "S";
        if (mAzimuth <= 170 && mAzimuth > 100)
            where = "SE";
        if (mAzimuth <= 100 && mAzimuth > 80)
            where = "E";
        if (mAzimuth <= 80 && mAzimuth > 10)
            where = "NE";


        txt_compass.setText(mAzimuth + "° " + where);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                noSensorsAlert();
            }
            else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
        else{
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }

    public void stop() {
        if (haveSensor) {
            mSensorManager.unregisterListener(this, mRotationV);
        }
        else {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mMagnetometer);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }


}
