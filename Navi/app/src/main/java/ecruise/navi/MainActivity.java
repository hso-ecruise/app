package ecruise.navi;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ecruise.common.ServerRequest;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{

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

    public void centerMarker()
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

    public void setMarkerPosition(LatLng pos, Marker marker)
    {
        marker.setPosition(pos);
    }

    public void setMarkerImage(String markerName, String imageType, LatLng ltlg)
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
