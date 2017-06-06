package me.ecruise.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import me.ecruise.data.ServerRequest;


public class Map2Activity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private ArrayList<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        actualizeMap();
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

                if(jObject.getInt("slotsOccupuied") < jObject.getInt("slots"))
                    setMarkerImage(jObject.getInt("slots") + " Slots", "freestation", cords);
                else
                    setMarkerImage(jObject.getInt("slots") + " Slots", "occupiedstation", cords);
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

    public void createMarker(String markerName, LatLng ltlg)
    {
        MarkerOptions mOpts = new MarkerOptions().position(ltlg).title(markerName);
        markers.add(mMap.addMarker(mOpts));
    }

    public void setMarkerImage(String markerName, String imageType, LatLng ltlg)
    {
        MarkerOptions mOpts = new MarkerOptions();

        if(imageType.equals("freestation"))
        {
            mOpts = new MarkerOptions().position(ltlg).title(markerName).snippet("Customized Marker").icon(BitmapDescriptorFactory.fromResource(R.mipmap.free_station));
        }
        /*ielse if(imageType.equals("owncar"))
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
        }*/
        markers.add(mMap.addMarker(mOpts));
    }
}
