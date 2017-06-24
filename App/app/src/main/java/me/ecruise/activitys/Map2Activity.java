package me.ecruise.activitys;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
    private Button mConfirmButton;
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
        mConfirmButton = (Button) findViewById(R.id.bookButton);
        mConfirmButton.setVisibility(View.INVISIBLE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapFragment.getMapAsync(this);

        Map.getInstance(this.getApplicationContext()).getCarsFromServer(new Customer.DataCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure() {

            }
        });
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

        final MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(0,0)).title("Gewählte Position").snippet("selectedPosition");
        final Marker bookingPosition = mMap.addMarker(markerOptions);

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
            if(Map.getInstance(this.getApplicationContext()).getBookedCarId() != 0) {
                Log.d("Show: ", "booked Car");
                showBookedCarMarker();
            }
            else{
                Log.d("Show: ", "booked Position");
                showBookedPositionMarker();
            }

        }
        if (Map.getInstance(this.getApplicationContext()).isGetLocation())
        {
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng point) {
                    // TODO Auto-generated method stub
                    bookingPosition.setPosition(point);
                }
            });


            mConfirmButton.setVisibility(View.VISIBLE);
            mConfirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(bookingPosition.getPosition().latitude != 0 && bookingPosition.getPosition().longitude != 0)
                    {
                        response(true, bookingPosition.getPosition());
                    }
                    else
                    {
                        noPosAlert();
                    }
                }
            });
        }
        else
        {
            mConfirmButton.setVisibility(View.INVISIBLE);
        }
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                bookingPosition.setPosition(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.

            }
        });
    }

    private void noPosAlert()
    {
        Log.d("Alert", "Failure");
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Keine Position gewählt, tippen sie auf die Karte um eine Position zu wählen!");
        dlgAlert.setTitle("Hinweis");
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void response(boolean success, LatLng position)
    {
        Intent resultData = new Intent();
        resultData.putExtra("longitude", position.longitude);
        resultData.putExtra("latitude", position.latitude);
        if(success)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
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
            Log.d("Show", "Car Nr" + Integer.toString(car.getId()));
            String snippet = "Car " + Integer.toString(car.getId()) + "\n Lädt noch" + car.getChargingMinutes() + "min.";
            createMarker(car.getName(), car.getPos(), snippet);
            setMarkerImage(snippet, "charging" + Integer.toString(car.getChargingLevel()));
        }
    }

    /**
     * shows Marker for the booked Car on the map
     */
    private void showBookedCarMarker() {
        Log.d("Show", "showBookedCarMarker");
        int id = Map.getInstance(this.getApplicationContext()).getBookedCarId();
        ArrayList<Car> cars = Map.getInstance(this.getApplicationContext()).getAllCars();
        for (Car car : cars) {
            Log.d("Car Nr", Integer.toString(car.getId()));
            if (id == car.getId()) {
                CameraPosition cp = new CameraPosition.Builder().target(car.getPos()).zoom(13).build();
                CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
                mMap.moveCamera(cu);
                createMarker(car.getName(), car.getPos(), car.getPlate());
                setMarkerImage(car.getPlate(), "owncar");
            }

        }
    }

    /**
     * shows Marker for the booked Position on the map
     */
    private void showBookedPositionMarker() {
        CameraPosition cp = new CameraPosition.Builder().target(Map.getInstance(this.getApplicationContext()).getBookedPos()).zoom(13).build();
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
        mMap.moveCamera(cu);
        createMarker("Buchung", Map.getInstance(this.getApplicationContext()).getBookedPos(), "Buchung" );
    }

    /**
     * This method create a marker on the GoogleMap
     *
     * @param markerName given Marker Title
     * @param ltlg       given Marker position
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
        if(currMarker == null)
        {
            return;
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
