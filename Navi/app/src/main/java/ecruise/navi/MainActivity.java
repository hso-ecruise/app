package ecruise.navi;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
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

import ecruise.common.DataParser;
import ecruise.common.ServerRequest;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{
    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
        private String downloadUrl(String strUrl) throws IOException
        {
            String data = "";
            InputStream iStream;
            HttpURLConnection urlConnection = null;

            try
            {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    //Log.d("downloadUrl:LINE: ", line);
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data);
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                assert urlConnection != null;
                urlConnection.disconnect();
            }

            return data;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++)
                {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                myRoute = points;

                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null)
            {
                Log.d("onPostExecute()","adding PolyLine to map!!");
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute()","without Polylines drawn");
            }
        }
    }

    private GoogleMap mMap;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<LatLng> myRoute = new ArrayList<>();
    private static int counterForRoute = 0;
    private static long timer = 0;
    private static boolean countStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button centerButton = (Button) findViewById(R.id.centerButton);
        centerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                centerMarker();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
    }

    private void centerMarker()
    {
        actualizeMap();
        drawRoute();
        moveCar();
    }


    private void moveCar()
    {
        Long value = timer;
        Log.d("TIMER", "CURRENT TIME =" + String.valueOf(value));
        counterForRoute = value.intValue();

        if(counterForRoute == 0 && myRoute.size() > 0)
        {
            if(!countStarted)
            {
                startCountDown();
                countStarted = true;
            }
            Log.d("MYROUTESIZE", String.valueOf(myRoute.size()));
            LatLng currPos = myRoute.get(counterForRoute);
            createMarker("My car", currPos, "TrueCarId");
            setMarkerImage("TrueCarId", "owncar");
            CameraPosition cp = new CameraPosition.Builder().target(currPos).zoom(16).build();
            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
            mMap.moveCamera(cu);
            counterForRoute++;
        }
        else if(counterForRoute < myRoute.size() && myRoute.size() > 0)
        {
            Log.d("moveCar()", "------------------DRAWING CAR----------------");
            LatLng currPos = myRoute.get(counterForRoute);
            createMarker("My car", currPos, "TrueCarId");
            setMarkerImage("TrueCarId", "owncar");
            setMarkerPosition(currPos, getMarkerOnSnippet("TrueCarId"));
            CameraPosition cp = new CameraPosition.Builder().target(currPos).zoom(16).build();
            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
            mMap.moveCamera(cu);
            counterForRoute++;
        }
        else
        {
            Log.d("moveCar()", "---------------SOMETHING WENT WRONG. DATA HERE = " + counterForRoute + ", " +  myRoute.size());
        }
    }
    private Marker getMarkerOnSnippet(String snippet)
    {
        for(int i = 0; i < markers.size(); i++)
        {
            if(markers.get(i).getSnippet().equals(snippet))
                return markers.get(i);
        }

        return null;
    }

    private void initAllMarkers(JSONArray jArray)
    {
        JSONObject jObject;
        LatLng cords;

        try
        {
            for (int i = 0; i < jArray.length(); i++)
            {
                jObject = (JSONObject) jArray.get(i);
                cords = new LatLng((Double) jObject.get("latitude"), (Double) jObject.get("longitude"));
                Log.d("initAllMarkers", cords.toString());

                createMarker(jObject.getInt("slots") + " Slots", cords, String.valueOf(jObject.getInt("chargingStationId")));

                if(jObject.getInt("slotsOccupied") < jObject.getInt("slots"))
                    setMarkerImage(String.valueOf(jObject.getInt("chargingStationId")), "freestation");
                else
                    setMarkerImage(String.valueOf(jObject.getInt("chargingStationId")), "occupiedstation");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    private void actualizeMap()
    {
        ServerRequest sr = new ServerRequest(this.getApplicationContext());
        sr.generateJsonArray("https://api.ecruise.me/v1/charging-stations",
                new ServerRequest.VolleyCallbackArray()
                {
                    @Override
                    public void onSuccess(JSONArray result)
                    {
                        try
                        {
                            initAllMarkers(result);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private String getUrl(LatLng orig, LatLng dest)
    {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + orig.latitude + ","  + orig.longitude  + "&destination="+ dest.latitude + "," + dest.longitude + "&sensor=false&units=metric&mode=driving&key=AIzaSyCJ32t4b_NZ1MY_dDW6XKf5hYLLZOddRVQ";
    }

    private boolean drawRoute()
    {
        LatLng origin = new LatLng(49.485000, 8.468000);
        LatLng dest = new LatLng(49.500000, 8.500000);

        // Getting URL to the Google Directions API
        String url = getUrl(origin, dest);
        Log.d("drawRoute()SETTING URL:", url);
        FetchUrl FetchUrl = new FetchUrl();
        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        return true;
    }

    private void createMarker(String markerName, LatLng ltlg, String snippetId)
    {
        boolean create = true;

        for(int j = 0; j < markers.size(); j++)
        {
            if (markers.get(j).getSnippet().equals(snippetId))
                create = false;
        }

        if(create)
        {
            MarkerOptions mOpts = new MarkerOptions().position(ltlg).title(markerName).snippet(snippetId);
            markers.add(mMap.addMarker(mOpts));
        }
    }

    private void setMarkerPosition(LatLng pos, Marker marker)
    {
        marker.setPosition(pos);
    }

    private void startCountDown()
    {
        new CountDownTimer(342000, 1000) {

            public void onTick(long millisUntilFinished) {
                timer = 171 - (millisUntilFinished / 2000);
            }

            public void onFinish() {
               Log.d("TIMER", "Timer done now!");
            }
        }.start();

    }

    private void setMarkerImage(String snippetId, String imageType)
    {
        Marker currMarker = null;

        for(int i = 0; i < markers.size(); i++)
        {
            Marker marker = markers.get(i);

            if(marker.getSnippet().equals(snippetId))
            {
                currMarker = marker;
                break;
            }
        }

        switch (imageType) {
            case "freestation":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.free_station_gmap));
                break;
            case "owncar":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.own_car));
                break;
            case "reservedcar":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.reserved_car));
                break;
            case "occupiedstation":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.occupied_station));
                break;
        }
    }

}
