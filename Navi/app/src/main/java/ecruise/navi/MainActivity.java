package ecruise.navi;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import org.json.JSONException;
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
    /**
     *  This class represents Url Fetching class. It calls the urls and passes data to next class
     **/
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try
            {
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
        /*
         *  This method dowloads the give URL and transform recieved data to string.
         *
         *  @param strUrl URL in string format
         *
         *  @return Data from url transformed into string
         */
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

    /**
    *  This class represents Parsing class. It parses data from Url and exceutes polyline drawing.
    **/
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try
            {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            }
            catch (Exception e)
            {
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
            else
                {
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
        final TextView textView = (TextView) findViewById(R.id.textView6);

        centerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    centerMarker();
                    textView.setText(String.valueOf(getBatteryStatus()*100) + "%");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
    }

    /**
     *  This executes on "Zentrieren" button click and actulize the map with
     *  data received from server. Also car simulation will be executed.
     **/
    private void centerMarker() throws JSONException
    {
        login();

        drawRoute();

        moveCar();

    }

    /**
     *  This method simulate car moving over the route. Internal timer is used to simulate the route moving.
     **/
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
        else if(counterForRoute == myRoute.size() && myRoute.size() > 0)
        {
            Log.d("moveCar()", "------------------DRAWING CAR----------------");
            LatLng currPos = myRoute.get(counterForRoute - 1);
            createMarker("My car", currPos, "TrueCarId");
            setMarkerImage("TrueCarId", "owncar");
            setMarkerPosition(currPos, getMarkerOnSnippet("TrueCarId"));
            CameraPosition cp = new CameraPosition.Builder().target(currPos).zoom(16).build();
            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
            mMap.moveCamera(cu);
        }
        else {
            Log.d("moveCar()", "---------------SOMETHING WENT WRONG. DATA HERE = " + counterForRoute + ", " + myRoute.size());
        }
    }

    /**
     *  This method get Marker on its snippet name
     *
     *  @param snippet Snippet name as string
     *
     *  @return Marker object
     **/
    private Marker getMarkerOnSnippet(String snippet)
    {
        for(int i = 0; i < markers.size(); i++)
        {
            if(markers.get(i).getSnippet().equals(snippet))
                return markers.get(i);
        }

        return null;
    }


    /**
     *  This method initialize all Stations received from backend server
     *
     *  @param jArray Json Array containing all station objects
     **/
    private void initAllStations(JSONArray jArray)
    {
        JSONObject jObject;
        LatLng cords;

        try
        {
            for (int i = 0; i < jArray.length(); i++)
            {
                jObject = (JSONObject) jArray.get(i);
                String stationId = "Station" + String.valueOf(jObject.getInt("chargingStationId"));
                cords = new LatLng((Double) jObject.get("latitude"), (Double) jObject.get("longitude"));
                Log.d("initAllStations()", cords.toString());

                createMarker(jObject.getInt("slots") + " Slots", cords, stationId);

                if(jObject.getInt("slotsOccupied") < jObject.getInt("slots"))
                    setMarkerImage(stationId, "freestation");
                else
                    setMarkerImage(stationId, "occupiedstation");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
    *  This method initialize all Cars received from backend server
    *
    *  @param jArray Json Array containing all car objects
    **/
    private void initAllCars(JSONArray jArray)
    {
        JSONObject jObject;
        LatLng cords;

        try
        {
            for (int i = 0; i < jArray.length(); i++)
            {
                jObject = (JSONObject) jArray.get(i);
                String carId = "Car" + String.valueOf(jObject.getInt("carId"));
                cords = new LatLng((Double) jObject.get("lastKnownPositionLatitude"), (Double) jObject.get("lastKnownPositionLongitude"));
                Log.d("initAllCars()", cords.toString());

                if(jObject.getInt("chargingState") != 1)
                    createMarker(jObject.getString("model"), cords, carId);

                if(jObject.getInt("chargingState") == 2)
                {
                    switch(getChargeLevel(jObject.getInt("chargeLevel"))) {
                        case 1:
                            setMarkerImage(carId, "charging0");
                            break;
                        case 2:
                            setMarkerImage(carId, "charging25");
                            break;
                        case 3:
                            setMarkerImage(carId, "charging50");
                            break;
                        case 4:
                            setMarkerImage(carId, "charging75");
                            break;
                        case 5:
                            setMarkerImage(carId, "charging100");
                            break;
                    }
                }
                else if(jObject.getInt("chargingState") == 3 && jObject.getInt("bookingState") == 1)
                    setMarkerImage(carId, "freecar");
                else if(jObject.getInt("chargingState") == 3 && jObject.getInt("bookingState") == 2)
                    setMarkerImage(carId, "reservedcar");
                else if(jObject.getInt("chargingState") == 3 && jObject.getInt("bookingState") == 3)
                    setMarkerImage(carId, "reservedcar");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void login()
    {
        ServerRequest login = new ServerRequest(this.getApplicationContext(), "");
        login.getToken(
                new ServerRequest.VolleyCallbackObject()
                {
                    @Override
                    public void onSuccess(JSONObject result)
                    {
                        try
                        {
                            requestServer(result.getString("token"));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
    }


    /**
    *  This method create Requests to server on the specified URL
    **/
    private void requestServer(String token) throws JSONException
    {
        ServerRequest sr = new ServerRequest(this.getApplicationContext(), token);
        sr.generateJsonArray("https://api.ecruise.me/v1/charging-stations",
                new ServerRequest.VolleyCallbackArray()
                {
                    @Override
                    public void onSuccess(JSONArray result)
                    {
                        try
                        {
                            Log.d("INITIATING ALL STATIONS", "");
                            initAllStations(result);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

        ServerRequest sr2 = new ServerRequest(this.getApplicationContext(), token);
        sr2.generateJsonArray("https://api.ecruise.me/v1/cars",
                new ServerRequest.VolleyCallbackArray()
                {
                    @Override
                    public void onSuccess(JSONArray result)
                    {
                        try
                        {
                            Log.d("INITIATING ALL CARS", "");
                            initAllCars(result);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
    *  This method map charging states of a car to 1-5 depending on chargelevel
    *
    *  @param chargeLevel this param represents exact chargeLevel of a car
    *
    *  @return mapped charge level
    **/
    private int getChargeLevel(int chargeLevel)
    {
        if(chargeLevel <= 25)
            return 1;
        if(chargeLevel > 25 && chargeLevel <= 50)
            return 2;
        if(chargeLevel > 50 && chargeLevel <= 75)
            return 3;
        if(chargeLevel > 75 && chargeLevel < 100)
            return 4;
        if(chargeLevel >= 100)
            return 5;

        return -1;
    }


    /**
    *  This method construct the Route URL for Google Directions API depending on given parameters
    *
    *  @param orig represents original car position
    *
    *  @param dest represents destination of the car
    *
    *  @return constructed URL
    **/
    private String getUrl(LatLng orig, LatLng dest)
    {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + orig.latitude + ","  + orig.longitude  + "&destination="+ dest.latitude + "," + dest.longitude + "&sensor=false&units=metric&mode=driving&key=AIzaSyCJ32t4b_NZ1MY_dDW6XKf5hYLLZOddRVQ";
    }


    /**
    *  This method draw a route for simulated driving
    *
    *  @return true on success, else false
    **/
    private void drawRoute()
    {
        LatLng origin = new LatLng(49.485000, 8.468000);
        LatLng dest = new LatLng(49.500000, 8.500000);

        // Getting URL to the Google Directions API
        String url = getUrl(origin, dest);
        Log.d("drawRoute()SETTING URL:", url);
        FetchUrl fetchUrl = new FetchUrl();
        // Start downloading json data from Google Directions API
        fetchUrl.execute(url);
    }


    /**
    *  This method create a marker on the GoogleMap
    *
    *  @param markerName given Marker Title
    *  @param ltlg given Marker positionm
    *  @param snippetId give Marker Snippet
    **/
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


    /**
    *  This method set the Marker position
    *
    *  @param pos new Position of the given marker
    *  @param marker the marker, which position should be changed
    *
    **/
    private void setMarkerPosition(LatLng pos, Marker marker)
    {
        marker.setPosition(pos);
    }


    /**
    *  This method starting internal countdown for simulation
    **/
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

    private float getBatteryStatus()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.getApplicationContext().registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        return batteryPct;
    }


    /**
    *  This method set the Marker Icon of a given marker Snippet ID
    *
    *  @param snippetId Marker id defined in the Snippet
    *  @param imageType the image type, specified in the method
    *
    *  @return mapped charge level
    **/
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
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.reserved_car_2));
                break;
            case "occupiedstation":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.occupied_station));
                break;
            case "freecar":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.free_car));
                break;
            case "blockedcar":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.car_blocked));
                break;
            case "charging0":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.charging_zero));
                break;
            case "charging25":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.charging_25));
                break;
            case "charging50":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.charging_50));
                break;
            case "charging75":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.charging_75));
                break;
            case "charging100":
                assert currMarker != null;
                currMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.charging_100));
                break;

        }
    }

}
