package com.pathways;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final float CAMERA_TILT = 90.0f;
    private GoogleMap mMap;
    private List polylinePoints= new ArrayList<LatLng>();
    //private int currentEmulatedLocation = 0;
    private Marker userMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng origin = new LatLng(28.480888, 77.094385);
        LatLng destination = new LatLng(28.450810, 77.099537);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 17));

        MarkerOptions options = new MarkerOptions();
        options.position(origin);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        //options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        userMarker = mMap.addMarker(options);

        String url = getDirectionsUrl(origin, destination);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            PolylineOptions lineOptions = null;
            if(null==polylinePoints){
                polylinePoints = new ArrayList<LatLng>();
            }else {
                polylinePoints.clear();
            }
            //currentEmulatedLocation = 0;

            for (int i = 0; i < result.size(); i++) {
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    polylinePoints.add(position);
                }

                lineOptions.addAll(polylinePoints);
                lineOptions.width(24);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }

            if(null!=lineOptions) {
                mMap.addPolyline(lineOptions);
            }

            emulateMarkerMove(0);
        }
    }

    private void emulateMarkerMove(final int currentEmulatedLocation){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(currentEmulatedLocation == polylinePoints.size()) {
                    return;
                }

                LatLng currentLocation = (LatLng) polylinePoints.get(currentEmulatedLocation);
                userMarker.setPosition(currentLocation);
                if(currentEmulatedLocation > 0) {
                    LatLng lastLocation = (LatLng) polylinePoints.get(currentEmulatedLocation-1);
                    OnLocationChange(currentLocation, lastLocation);
                }

                emulateMarkerMove(currentEmulatedLocation+1);
            }
        }, 2000);
    }

    public void OnLocationChange(LatLng location, LatLng lastLocation) {

        if (mMap == null || location == null) {
            return;
        }

        double bearing = 0;
        if(lastLocation != null){

            double lat1 = lastLocation.latitude;
            double lng1 = lastLocation.longitude;

            double lat2 = location.latitude;
            double lng2 = location.longitude;

            double dLon = (lng2-lng1);
            double y = Math.sin(dLon) * Math.cos(lat2);
            double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
            bearing = Math.toDegrees((Math.atan2(y, x)));
            bearing = (360 - ((bearing + 360) % 360));
        }

        CameraPosition cameraPosition = CameraPosition.builder().
                tilt(CAMERA_TILT).
                bearing((float) bearing).
                zoom(mMap.getCameraPosition().zoom).
                target(location).
                build();


        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}