package com.pathways;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapzen.speakerbox.Speakerbox;
import com.pathways.conversation.TTS;
import com.pathways.conversation.UtteranceCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, UtteranceCompleteListener {

    private static final float CAMERA_TILT = 90.0f;
    private GoogleMap mMap;
    private List<PolylinePoint> polylinePoints = new ArrayList<>();
    private Marker userMarker;
    private double oldBearing = 0;
    List<LatLongObject> data = new ArrayList<>();
    PolylinePoint currentPosition;
    boolean isSpeaking = false;
    private AudioManager audioManager;
    public int currentEmulatedLoc;

    private String jsonString = "[\n" +
            "  {\n" +
            "    \"Event\": \"Bristol Chawk\",\n" +
            "    \"Latitude\": 28.478886,\n" +
            "    \"Longitude\": 77.093913,\n" +
            "    \"Commentary\": \"Hi, I am your personal assistant Pathways for site tour. I will be assisting you in reaching to DLF Camellias.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"Golf Course Road starts from Bristol Chowk and goes up to Sector 56. It is a stretch of 6.7 kilometers with major localities of Gurgaon that is DLF Phase 1, DLF Phase 5, Sector 54, 56 and many more.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"Beginning\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"There are more than 170 real estate projects from more than 40 Builders on Golf Course Road.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"DLF Mega Mall\",\n" +
            "    \"Latitude\": 28.475849,\n" +
            "    \"Longitude\": 77.093103,\n" +
            "    \"Commentary\": \"The commercial space in Golf Course Road is quite prominent. You can see DLF Mega Mall on your right side which is one of the popular Mall in this locality.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"Beginning\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"Prices for properties in Golf Course Road ranges between 9 thousands and 18.5 thousands Indian Rupees per square feet for Buy. The average price per square feet for this locality is 56% higher than the average price for entire Gurgaon city.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"Beginning\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"The average price for this locality has witnessed a growth of 95% in last 10 years however a decline of 11% in last 5 years. The highest price has been 14.5 thousands per square feet in 2015.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"Beginning\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"The Golf Course Road has around 20 schools and more than 7 hospitals locating within 6.7 km of the area.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"Beginning\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"This locality is known for its luxury service apartments and has easy proximity to the Delhi.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"Sector 27\",\n" +
            "    \"Latitude\": 28.448394,\n" +
            "    \"Longitude\": 77.099961,\n" +
            "    \"Commentary\": \"On your right side there is a Sector 27. It is a popular residential locality in Gurgaon which offers necessary civic and social infrastructures.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"Sector 27\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"Average price per square feet for Sector 27 is 11 thousands for Buy.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"DLF Phase 1\",\n" +
            "    \"Latitude\": 28.471041,\n" +
            "    \"Longitude\": 77.094151,\n" +
            "    \"Commentary\": \"Now you are passing through DLF Phase 1 on your left side where the average price per square feet for Buy is 16 thousands. The per square feet price ranges between 12 thousands and 26 thousands in this locality.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"There has been an appreciation of 87% and 59% in price per square feet for DLF Phase 1 in last 10 years and 5 years respectively.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"Sector 42-43 Rapid Metro Station\",\n" +
            "    \"Latitude\": 28.457416,\n" +
            "    \"Longitude\": 77.096955,\n" +
            "    \"Commentary\": \"Here you are passing through Rapid Metro station Sector 42-43. Rapid Metro started on Golf Course Road in November 2013. It has a total length of 11.7 kilometers which connects all major sectors of Golf Course Road with center of the city and further with near by city, Delhi.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"Sector 43 on your left has shown a price appreciation of 155% in last 10 years but a decline of 11% in last 5 years for buy.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"The average price per square feet in Sector 43 is 10 thousand 8 hundred Indian Rupees.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"There are around 20 residential projects in this locality including popular projects like DLF The Icon, DLF Pinnacle and Ansal Maple Heights.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"DLF Camellias\",\n" +
            "    \"Latitude\": 28.449882,\n" +
            "    \"Longitude\": 77.0960453,\n" +
            "    \"Commentary\": \"Hey now you are reaching your destination DLF Camellias in Sector 42.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"DLF Camellias is a residential project by DLF Group offering all modern amenities like an exclusive club with swimming pool, steam and sauna facilities, Joggers Park, Tennis and Squash courts and other leisure and fitness facilities.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"It is spread across 18 acres of land with 75% of green space.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"The project is offering 12 hundred flats with 4,5 and 6 BHK in 16 Towers of 39 floors each.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"and the flats sizes range between 7196 and 7400 squate feet. The price of flats starts from 195 million and goes up to 257 millions per flat in DLF Camellias.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"It was launched in 2014 at a price of 25 thousand 5 hunder per square feet which has been appreciated by 6% as of date and presently hovering around 27 thousands.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"DLF Camellias has close proximity to schools, hospitals, banks, malls and metro stations.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"Paras Hospital is located at 3.4 kilometers from the project. Delhi Public School is situated at the distance of 6 kilometers and nearest bank is just a 2.4 kilometers away.\",\n" +
            "    \"\": \"\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"Event\": \"\",\n" +
            "    \"Latitude\": \"do\",\n" +
            "    \"Longitude\": \"do\",\n" +
            "    \"Commentary\": \"We are ending our site tour here. Please let me know if you want to know more about the project.\",\n" +
            "    \"\": \"\"\n" +
            "  }\n" +
            "]";
    private ImageView button;
    private boolean isPaused = false;
    private int currentVolume = 50;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTTS();
        constructObject();
        setContentView(R.layout.activity_maps);
        button = (ImageView) findViewById(R.id.button);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPaused = !isPaused;
                if (!isPaused) {
                    button.setImageResource(R.drawable.if_music_pause_stop_control_play_blue_1872770);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    emulateMarkerMove(currentEmulatedLoc);
                } else {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    button.setImageResource(R.drawable.if_music_play_pause_control_go_arrow_blue_1872769);
                    handler.removeCallbacksAndMessages(null);
                }
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void constructObject() {
        LatLongObject object = new LatLongObject();
        object.setLat(28.478886);
        object.setLog(77.093913);
        object.setLabel("Bristol Chawk");
        List<String> list = new ArrayList<>();
        list.add("Hi, I am your personal assistant Pathways for site tour. I will be assisting you in reaching to DLF Camellias.");
        object.setSpeechList(list);


        data.add(object);
        object = new LatLongObject();
        object.setLat(28.475849);
        object.setLog(77.093103);
        object.setLabel("DLF Mega Mall");
        list = new ArrayList<>();
        list.add("The commercial space in Golf Course Road is quite prominent. You can see DLF Mega Mall on your right side which is one of the popular Mall in this locality.");
        object.setSpeechList(list);

        data.add(object);
        object = new LatLongObject();
        object.setLat(28.471313);
        object.setLog(77.093897);
        object.setLabel("DLF Phase 1");
        list = new ArrayList<>();
        list.add("Now you are passing through DLF Phase 1 on your left side where the average price per square feet for Buy is 16 thousands. The per square feet price ranges between 12 thousands and 26 thousands in this locality.");
        object.setSpeechList(list);

        data.add(object);
        object = new LatLongObject();
        object.setLat(28.466140);
        object.setLog(77.094125);
        object.setLabel("Sector 27");
        list = new ArrayList<>();
        list.add("On your right side there is a Sector 27. It is a popular residential locality in Gurgaon which offers necessary civic and social infrastructures.");
        object.setSpeechList(list);

        data.add(object);
        object = new LatLongObject();
        object.setLat(28.457448);
        object.setLog(77.096944);
        object.setLabel("Sector 42-43 Rapid Metro Station");
        list = new ArrayList<>();
        list.add("Here you are passing through Rapid Metro station Sector 42-43. Rapid Metro started on Golf Course Road in November 2013. It has a total length of 11.7 kilometers which connects all major sectors of Golf Course Road with center of the city and further with near by city, Delhi.");
        object.setSpeechList(list);

        data.add(object);
        object = new LatLongObject();
        object.setLat(28.450810);
        object.setLog(77.099537);
        object.setLabel("DLF Camellias");
        list = new ArrayList<>();
        list.add("Hey now you are reaching your destination DLF Camellias in Sector 42.");
        object.setSpeechList(list);

        data.add(object);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng origin = new LatLng(28.480888, 77.094385);
        LatLng destination = new LatLng(28.450810, 77.099537);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 17));

        MarkerOptions options = new MarkerOptions();
        options.position(origin);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
        //options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        userMarker = mMap.addMarker(options);

        String url = getDirectionsUrl(origin, destination);
        ParserTask parserTask = new ParserTask();
        parserTask.execute();
    }

    @Override
    public void onUtteranceComplete() {
        isSpeaking = false;
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
                jObject = new JSONObject(Constants.directionJson);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<PolylinePoint> result) {
            if (null == result || result.isEmpty()) {
                return;
            }

            PolylineOptions lineOptions = null;
            List<LatLng> points = new ArrayList<>();
            if (null == polylinePoints) {
                polylinePoints = new ArrayList<>();
            } else {
                polylinePoints.clear();
            }


            polylinePoints.addAll(result);

            initMapDirection();


            for (int i = 0; i < result.size(); i++) {
                lineOptions = new PolylineOptions();

                PolylinePoint point = result.get(i);
                points.add(point.latLng);

                lineOptions.addAll(points);
                lineOptions.width(30);
                lineOptions.color(Color.parseColor("#0080ff"));
                lineOptions.geodesic(true);
            }

            if (null != lineOptions) {
                mMap.addPolyline(lineOptions);
            }
            emulateMarkerMove(0);
        }
    }

    private void initMapDirection() {
        if (polylinePoints.isEmpty()) {
            return;
        }
        PolylinePoint firstPoint = polylinePoints.get(0);
        PolylinePoint secondPoint = polylinePoints.get(1);
        realignMap(firstPoint, secondPoint);
    }

    private void emulateMarkerMove(final int currentEmulatedLocation) {
        if (currentEmulatedLocation == polylinePoints.size()) {
            return;
        }
        if (getNextPoint() != null && shouldStartSpeech(getNextPoint()) && !isSpeaking) {
            LatLng marker = new LatLng((double) getNextPoint().getLat(), getNextPoint().getLog());
            addMarker(marker, R.color.icon_orange_color, false, getNextPoint().getLabel());
            speak(getCommentory(getNextPoint()));
            getNextPoint().setPassed(true);
            isSpeaking = true;
        }

        final int duration = polylinePoints.get(currentEmulatedLocation).duration;
        Log.e("emulateMarkerMove : ", " " + duration);

        handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.i("Maps", "" + isPaused);
                if (!isPaused) {
                    PolylinePoint nextPosition = polylinePoints.get(currentEmulatedLocation);

                    if (currentEmulatedLocation > 0) {
                        currentPosition = polylinePoints.get(currentEmulatedLocation - 1);
                        onMarkerLocationChange(currentPosition.latLng, nextPosition.latLng, userMarker, duration);
                        realignMap(currentPosition, nextPosition);
                    }
                    currentEmulatedLoc = currentEmulatedLocation + 1;
                    Log.i("Maps", "" + currentEmulatedLocation);
                    emulateMarkerMove(currentEmulatedLocation + 1);
                }
            }
        }, duration);
    }

    private String getCommentory(LatLongObject nextPoint) {
        return nextPoint.getSpeechList().get(0);
    }

    private boolean shouldStartSpeech(LatLongObject nextPoint) {
        if (nextPoint != null && nextPoint.getLat() > 0.0 && nextPoint.getLog() > 0.0 &&
                currentPosition != null && currentPosition.latLng != null && currentPosition.latLng.latitude > 0.0 &&
                currentPosition.latLng.longitude > 0.0) {
            double distance = meterDistanceBetweenPoints(nextPoint.getLat(), nextPoint.getLog(), currentPosition.latLng.latitude, currentPosition.latLng.longitude);
            return distance < 250;
        }
        return false;
    }

    private LatLongObject getNextPoint() {
        LatLongObject newObject = null;
        for (LatLongObject object : data) {
            if (!object.isPassed()) {
                newObject = object;
                break;
            }
        }
        return newObject;
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


    void initTTS() {
        TTS.init(this, this);
    }

    private void speak(String speech) {
        TTS.speak(speech);
    }


    private void onMarkerLocationChange(final LatLng startPosition, final LatLng nextPosition, final Marker mMarker, final int duration) {

        final Handler handler = new Handler();
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

    private void playSpeech(final int position) {
        try {
            final JSONArray jsonArray = new JSONArray(jsonString);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Speakerbox speakerbox = new Speakerbox(getApplication());
                        JSONObject jsonObject = new JSONObject(jsonArray.get(position).toString());
                        //  speakerbox.play(jsonObject.getString("Commentary"));
                        speak(jsonObject.getString("Commentary"));
                        if (!jsonObject.get("Latitude").equals("do") || !jsonObject.get("Longitude").equals("do")) {
                            LatLng marker = new LatLng((double) jsonObject.get("Latitude"), (double) jsonObject.get("Longitude"));
                            addMarker(marker, R.color.icon_orange_color, false, jsonObject.getString("Event"));
                        }
                    } catch (JSONException e) {

                    }
                    // playSpeech(position + 1);
                }
            }, 4000);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    protected Marker addMarker(LatLng position, @ColorRes int color, boolean draggable, String title) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(draggable);
        // markerOptions.icon(MapUtils.getIconBitmapDescriptor(getActivity(), color));
        markerOptions.position(position);
        Marker pinnedMarker = mMap.addMarker(markerOptions);
        pinnedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.info_marker));
        pinnedMarker.setTitle(title);
        pinnedMarker.showInfoWindow();
        startDropMarkerAnimation(pinnedMarker);
        return pinnedMarker;
    }

    private void startDropMarkerAnimation(final Marker marker) {
        final LatLng target = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point targetPoint = proj.toScreenLocation(target);
        final long duration = (long) (200 + (targetPoint.y * 0.6));
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        startPoint.y = 0;
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final Interpolator interpolator = new LinearOutSlowInInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * target.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * target.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 16ms later == 60 frames per second
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        float pk = (float) (180.f / Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

}