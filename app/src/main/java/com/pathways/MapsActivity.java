package com.pathways;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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

    private static final float CAMERA_TILT = 90.0f;
    private GoogleMap mMap;
    private List<PolylinePoint> polylinePoints= new ArrayList<>();
    private Marker userMarker;
    private double oldBearing = 0;

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
    private class ParserTask extends AsyncTask<String, Integer, List<PolylinePoint>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<PolylinePoint> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<PolylinePoint> routes = null;

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
        protected void onPostExecute(List<PolylinePoint> result) {
            if(null==result || result.isEmpty()){
                return;
            }
            PolylineOptions lineOptions = null;
            List<LatLng> points = new ArrayList<>();
            if(null==polylinePoints){
                polylinePoints = new ArrayList<>();
            }else {
                polylinePoints.clear();
            }

            polylinePoints.addAll(result);

            initMapDirection();

            for (int i = 0; i < result.size(); i++) {
                lineOptions = new PolylineOptions();

                PolylinePoint point = result.get(i);
                points.add(point.latLng);

                lineOptions.addAll(points);
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

    private void initMapDirection(){
        if(polylinePoints.isEmpty()){
            return;
        }
        PolylinePoint firstPoint = polylinePoints.get(0);
        PolylinePoint secondPoint = polylinePoints.get(1);
        realignMap(firstPoint, secondPoint);
    }
    private void emulateMarkerMove(final int currentEmulatedLocation){
        if(currentEmulatedLocation == polylinePoints.size()) {
            return;
        }

        final int duration = polylinePoints.get(currentEmulatedLocation).duration;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PolylinePoint nextPosition = polylinePoints.get(currentEmulatedLocation);

                if(currentEmulatedLocation > 0) {
                    PolylinePoint currentPosition = polylinePoints.get(currentEmulatedLocation-1);
                    onMarkerLocationChange(currentPosition.latLng, nextPosition.latLng, userMarker, duration);
                    realignMap(currentPosition, nextPosition);
                }

                emulateMarkerMove(currentEmulatedLocation+1);
            }
        }, duration);
    }

    public void realignMap(PolylinePoint currentPoint, PolylinePoint nextPoint) {

        if (mMap == null || currentPoint == null || nextPoint == null) {
            return;
        }

        CameraPosition cameraPosition = CameraPosition.builder().
                tilt(CAMERA_TILT).
                bearing((float) nextPoint.bearing).
                zoom(mMap.getCameraPosition().zoom).
                target(nextPoint.latLng).
                build();


        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void onMarkerLocationChange(final LatLng startPosition, final LatLng nextPosition, final Marker mMarker, final int duration) {

        final Handler handler =  new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = duration;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + nextPosition.latitude * t,
                        startPosition.longitude * (1 - t) + nextPosition.longitude * t);

                mMarker.setPosition(currentPosition);

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        mMarker.setVisible(false);
                    } else {
                        mMarker.setVisible(true);
                    }
                }
            }
        });

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