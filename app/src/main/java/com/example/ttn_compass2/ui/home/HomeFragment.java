package com.example.ttn_compass2.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.ttn_compass2.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    MapView mMapView;
    View mView;

    Location mlocation;
    FusedLocationProviderClient mFusedLocationClient;

    private static final int Request_code = 101;
    GoogleApiClient mGoogleApiClient;

    // Latitude & Longitude
    private Double Latitude = 0.00;
    private Double Longitude = 0.00;

    ArrayList<HashMap<String, String>> gateway_location = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;


    ImageView compass_img;
    TextView txt_compass;
    Spinner spin;
    int mAzimuth;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        //GetLastLocation();

        String[] country_list = {"Italy", "Spain", "France", "Ethiopia"};

        compass_img = (ImageView) mView.findViewById(R.id.img_compass);
        txt_compass = (TextView) mView.findViewById(R.id.txt_compass);
        spin = (Spinner) mView.findViewById(R.id.txtGatewaySearch);

        ArrayAdapter<String> adapterx = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, country_list);
        adapterx.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapterx);


        String country = "Italy";


        //LatLng latLng = getCurrentLocation();

        double lat1 = 45.703800;
        double lon1 = 13.720177;

        //double lat1 = latLng.latitude;
        //double lon1 = latLng.longitude;

        String unit = "k";

        try {

            JSONObject obj = new JSONObject(loadJSONFromAsset(country));

            JSONObject obj2 = (JSONObject) obj.get("ttn_node");
            Iterator iterator = obj2.keys();
            String key = null;
            int num_node = 0;

            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();

            HashMap<String, String> m_li;

            while (iterator.hasNext()) {
                num_node = num_node + 1;
                key = (String) iterator.next();

                String id = "" + ((JSONObject) obj2.get(key)).get("id");
                String location = "" + ((JSONObject) obj2.get(key)).get("location");
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
                String loc = lat + "," + lon;

                double lat2 = Double.parseDouble(lat);
                double lon2 = Double.parseDouble(lon);


                double d = distance(lat1, lon1, lat2, lon2, unit);
                String dis = Double.toString(d) + " km";

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

        return mView;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {

            double R = 6371;

            double φ1 = Math.toRadians(lat1);
            double φ2 = Math.toRadians(lat2);
            double Δφ = Math.toRadians(lat2 - lat1);
            double Δλ = Math.toRadians(lon2 - lon1);

            double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                    Math.cos(φ1) * Math.cos(φ2) *
                            Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            double dist = R * c;

            /*

            // Haversine formula
            */
            DecimalFormat df2 = new DecimalFormat("###.##");
            return (Double.valueOf(df2.format(dist)));
        }
    }

    public String loadJSONFromAsset(String country) {
        String json = null;


        try {
            if (country.equals("Italy")) {
                final InputStream is = getActivity().getApplicationContext().getAssets().open("ttn_italy.json");

                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");

                JSONObject obj = new JSONObject(json);
            } else if (country.equals("Spain")) {
                final InputStream is = getActivity().getApplicationContext().getAssets().open("ttn_spain.json");

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }


        // *** Marker (Loop)

        for (int i = 0; i < gateway_location.size(); i++) {
            Latitude = Double.parseDouble(gateway_location.get(i).get("Latitude").toString());
            Longitude = Double.parseDouble(gateway_location.get(i).get("Longitude").toString());
            String name = gateway_location.get(i).get("LocationName").toString();
            String distance = gateway_location.get(i).get("Distance").toString();

            MarkerOptions marker = new MarkerOptions().position(
                    new LatLng(Latitude, Longitude)).title(name + "\n" +
                    "Altitude (m):\n" + distance).snippet("Distance: " + distance + ", (" + Latitude + "," + Longitude + ")");
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.gateway));
            googleMap.addMarker(marker);

        }

        /*

        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);
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
        Location location = locationManager.getLastKnownLocation(provider);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        */
        LatLng latLng = new LatLng(45.703800, 13.720177);
        //45.703800, 13.720177

        String loc = latLng.toString();

        MapsInitializer.initialize(getContext());
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // MAP_TYPE_NORMAL
        googleMap.addMarker(new MarkerOptions().position(latLng).title("This is your place").snippet(loc));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private int checkSelfPermission(String accessFineLocation) {
        return 0;
    }

    public LatLng getCurrentLocation() {

        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);


        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng  = new LatLng(latitude, longitude);

        return latLng;

    }

    public void onRequestPermissionResult(int requestCode, String [] permissions, int[] grantResult){
        switch (requestCode){
            case Request_code:
                if(grantResult.length>0 && grantResult[0] == PackageManager.PERMISSION_GRANTED){
                    //GetLastLocation();
                }
                break;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}