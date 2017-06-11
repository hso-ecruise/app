package me.ecruise.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import me.ecruise.data.Car;
import me.ecruise.data.Customer;
import me.ecruise.data.Map;
import me.ecruise.data.Station;


public class Map2Activity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Marker> markers = new ArrayList<>();
    private static final LatLng centralPos = new LatLng(49.487155, 8.466219);

    /**
     * initializes the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

    }

    /**
     * initializes the google map
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        CameraPosition cp = new CameraPosition.Builder().target(centralPos).zoom(13).build();
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
        mMap.moveCamera(cu);
        if (Map.getInstance(this.getApplicationContext()).getShowStations()) {
            Map.getInstance(this.getApplicationContext()).getStationsFromServer(new Customer.DataCallback() {
                @Override
                public void onSuccess() {
                    showStationMarkers();
                }

                @Override
                public void onFailure() {

                }
            });
        }
        if (Map.getInstance(this.getApplicationContext()).getShowAllCars()) {
            Map.getInstance(this.getApplicationContext()).getCarsFromServer(new Customer.DataCallback() {
                @Override
                public void onSuccess() {
                    showCarMarkers();
                }

                @Override
                public void onFailure() {

                }
            });
        }
        if (Map.getInstance(this.getApplicationContext()).getShowBookedCar()) {
            Map.getInstance(this.getApplicationContext()).getCarsFromServer(new Customer.DataCallback() {
                @Override
                public void onSuccess() {
                    showBookedCarMarker();
                }

                @Override
                public void onFailure() {

                }
            });
        }
    }

    /**
     * shows Markers for Stations on the map
     */
    private void showStationMarkers() {
        ArrayList<Station> stations = Map.getInstance(this.getApplicationContext()).getStations();
        for (Station station : stations) {
            createMarker(station.getName(), station.getPos(), "Station " + Integer.toString(station.getId()));
            if (station.isFree())
                setMarkerImage("Station " + Integer.toString(station.getId()), "freestation");
            else
                setMarkerImage("Station " + Integer.toString(station.getId()), "occupiedstation");
        }
    }

    /**
     * shows Markers for Cars on the map
     */
    private void showCarMarkers() {
        ArrayList<Car> cars = Map.getInstance(this.getApplicationContext()).getCars();
        for (Car car : cars) {
            createMarker(car.getName(), car.getPos(), "Car " + Integer.toString(car.getId()));
            setMarkerImage("Car " + Integer.toString(car.getId()), "charging" + Integer.toString(car.getChargingLevel()));
        }
    }

    /**
     * shows Marker for the booked Car on the map
     */
    private void showBookedCarMarker() {
        int id = Map.getInstance(this.getApplicationContext()).getBookedCarId();
        ArrayList<Car> cars = Map.getInstance(this.getApplicationContext()).getCars();
        for (Car car : cars) {
            if (id == car.getId()) {
                createMarker(car.getName(), car.getPos(), "Car " + Integer.toString(car.getId()));
                setMarkerImage("Car " + Integer.toString(car.getId()), "charging" + Integer.toString(car.getChargingLevel()));
            }

        }
    }

    /**
     * This method create a marker on the GoogleMap
     *
     * @param markerName given Marker Title
     * @param ltlg       given Marker positionm
     * @param snippetId  give Marker Snippet
     **/
    private void createMarker(String markerName, LatLng ltlg, String snippetId) {
        boolean create = true;

        for (int j = 0; j < markers.size(); j++) {
            if (markers.get(j).getSnippet().equals(snippetId))
                create = false;
        }

        if (create) {
            MarkerOptions mOpts = new MarkerOptions().position(ltlg).title(markerName).snippet(snippetId);
            markers.add(mMap.addMarker(mOpts));
        }
    }

    /**
     * This method set the Marker Icon of a given marker Snippet ID
     *
     * @param snippetId Marker id defined in the Snippet
     * @param imageType the image type, specified in the method
     * @return mapped charge level
     **/
    private void setMarkerImage(String snippetId, String imageType) {
        Marker currMarker = null;

        for (int i = 0; i < markers.size(); i++) {
            Marker marker = markers.get(i);

            if (marker.getSnippet().equals(snippetId)) {
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
