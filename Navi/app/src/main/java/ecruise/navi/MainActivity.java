package ecruise.navi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;

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
        markers.add(googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(15, 25))
                .title("Marker")));
        mMap = googleMap;
    }

    public void centerMarker()
    {
        double latit = 0, longit = 0;

        //mMap.animateCamera(CameraUpdateFactory.zoomIn());
        for(Marker marker : markers)
        {
            latit = marker.getPosition().latitude;
            longit = marker.getPosition().longitude;
        }

        LatLng ltlg = new LatLng(latit, longit);
        LatLng newLtLg = new LatLng(18, 28);

        createMarker("New Marker", new LatLng(14, 24));

        for(Marker marker : markers)
        {
           if(marker.getTitle().equals("New Marker"))
               setMarkerPosition(newLtLg, marker);
        }

        LatLng custLtLg = new LatLng(10, 29);
        setMarkerImage("One More Marker", custLtLg);

        /*CameraPosition cp = new CameraPosition.Builder().target(ltlg).zoom(4).build();
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);*/
        CameraPosition cp = new CameraPosition.Builder().target(newLtLg).zoom(4).build();
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
        mMap.moveCamera(cu);
    }

    public LatLng getCords()
    {
        return null;
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

    public void setMarkerImage(String markerName, LatLng ltlg)
    {
        MarkerOptions mOpts = new MarkerOptions().position(ltlg).title(markerName).snippet("Customized Marker") .icon(BitmapDescriptorFactory.fromResource(R.mipmap.free_station_gmap));
        markers.add(mMap.addMarker(mOpts));
    }
}
