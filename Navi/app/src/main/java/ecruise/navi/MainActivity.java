package ecruise.navi;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
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
                Log.d("Background Task data", data.toString());
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
            InputStream iStream = null;
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

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    //Log.d("downloadUrl:LINE: ", line);
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
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
                Log.d("ParserTask",jsonData[0].toString());
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

        createMarker("My car", new LatLng(49.485, 8.468));
        setMarkerImage("My car", "owncar", new LatLng(49.485, 8.468));
        CameraPosition cp = new CameraPosition.Builder().target(new LatLng(49.485, 8.468)).zoom(18).build();
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
        mMap.moveCamera(cu);
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
                createMarker(jObject.getInt("slots") + " Slots", cords);

                if(jObject.getInt("slotsOccupied") < jObject.getInt("slots"))
                    setMarkerImage(jObject.getInt("slots") + " Slots", "freestation", cords);
                else
                    setMarkerImage(jObject.getInt("slots") + " Slots", "occupiedstation", cords);
            }

            drawRoute();
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
                            JSONArray jArray = result;
                            initAllMarkers(jArray);
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
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + orig.latitude + ","  + orig.longitude  + "&destination="+ dest.latitude + "," + dest.longitude + "&sensor=false&units=metric&mode=driving&key=AIzaSyCJ32t4b_NZ1MY_dDW6XKf5hYLLZOddRVQ";
        return url;
    }

    private void drawRoute()
    {

        LatLng origin = new LatLng(49.485000, 8.468000);
        LatLng dest = new LatLng(49.500000, 8.500000);

        // Getting URL to the Google Directions API
        String url = getUrl(origin, dest);
        Log.d("drawRoute()SETTING URL:", url.toString());
        FetchUrl FetchUrl = new FetchUrl();
        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

    private void createMarker(String markerName, LatLng ltlg)
    {
        MarkerOptions mOpts = new MarkerOptions().position(ltlg).title(markerName);
        markers.add(mMap.addMarker(mOpts));
    }

    private void setMarkerPosition(LatLng pos, Marker marker)
    {
        marker.setPosition(pos);
    }

    private void setMarkerImage(String markerName, String imageType, LatLng ltlg)
    {
        MarkerOptions mOpts = new MarkerOptions();

        if(imageType.equals("freestation"))
        {
            mOpts = new MarkerOptions().position(ltlg).title(markerName).snippet("Customized Marker").icon(BitmapDescriptorFactory.fromResource(R.mipmap.free_station_gmap));
        }
        else if(imageType.equals("owncar"))
        {
            mOpts = new MarkerOptions().position(ltlg).title(markerName).snippet("Customized Marker").icon(BitmapDescriptorFactory.fromResource(R.mipmap.own_car));
        }
        else if(imageType.equals("reservedcar"))
        {
            mOpts = new MarkerOptions().position(ltlg).title(markerName).snippet("Customized Marker").icon(BitmapDescriptorFactory.fromResource(R.mipmap.reserved_car));
        }
        else if(imageType.equals("occupiedstation"))
        {
            mOpts = new MarkerOptions().position(ltlg).title(markerName).snippet("Customized Marker").icon(BitmapDescriptorFactory.fromResource(R.mipmap.occupied_station));
        }
        markers.add(mMap.addMarker(mOpts));
    }

}
