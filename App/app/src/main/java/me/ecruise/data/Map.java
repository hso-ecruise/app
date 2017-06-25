package me.ecruise.data;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jens Ullrich on 07.06.2017.
 */
public class Map {
    private static Map mInstance;
    private static Context mCtx;
    private ArrayList<Station> stations = new ArrayList<>();
    private ArrayList<Car> cars = new ArrayList<>();
    private ArrayList<Car> allCars = new ArrayList<>();
    private boolean showStations = false;
    private boolean showBookedCar = false;
    private boolean showAllCars = false;
    private LatLng bookedPos;
    private int bookedCarId = 0;

    public ArrayList<Car> getAllCars() {
        return allCars;
    }

    public void setAllCars(ArrayList<Car> allCars) {
        this.allCars = allCars;
    }

    public int getBookedCarId() {
        return bookedCarId;
    }

    public void setBookedCarId(int bookedCarId) {
        this.bookedCarId = bookedCarId;
    }


    public boolean getShowStations() {
        return showStations;
    }

    public void setShowStations(boolean showStations) {
        this.showStations = showStations;
    }

    public boolean getShowBookedCar() {
        return showBookedCar;
    }

    public void setShowBookedCar(boolean showOwnCar) {
        this.showBookedCar = showOwnCar;
    }

    public boolean getShowAllCars() {
        return showAllCars;
    }

    public void setShowAllCars(boolean showAllCars) {
        this.showAllCars = showAllCars;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    public ArrayList<Car> getCars() {
        return cars;
    }


    public LatLng getBookedPos() {
        return bookedPos;
    }

    public void setBookedPos(LatLng bookedPos) {
        this.bookedPos = bookedPos;
    }

    public boolean isGetLocation() {
        return getLocation;
    }

    public void setGetLocation(boolean getLocation) {
        this.getLocation = getLocation;
    }

    private boolean getLocation = false;

    /**
     * @param ctx
     * @return
     */
    public static synchronized Map getInstance(Context ctx) {
        if (mInstance == null) {
            mCtx = ctx;
            mInstance = new Map();
        }
        return mInstance;
    }

    /**
     * @param callback
     */
    public void getStationsFromServer(Customer.DataCallback callback) {
        final Customer.DataCallback mCallback = callback;
        String url = "https://api.ecruise.me/v1/charging-stations";
        final String mToken = Customer.getInstance(mCtx).getToken();
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray array) {
                        saveStations(array, mCallback);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> params = new HashMap<>();
                params.put("access_token", mToken);
                return params;
            }
        };
        Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }

    /**
     * @param callback
     */
    public void getCarsFromServer(Customer.DataCallback callback) {
        final Customer.DataCallback mCallback = callback;
        String url = "https://api.ecruise.me/v1/cars";
        final String mToken = Customer.getInstance(mCtx).getToken();
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray array) {
                        saveCars(array, mCallback);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> params = new HashMap<>();
                params.put("access_token", mToken);
                return params;
            }
        };
        Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }

    /**
     * @param array
     * @param callback
     */
    public void saveStations(JSONArray array, Customer.DataCallback callback) {
        final Customer.DataCallback mCallback = callback;
        for (int i = 0; i < array.length(); i++) {
            Station station;
            try {
                LatLng pos = new LatLng(array.getJSONObject(i).getDouble("latitude"), array.getJSONObject(i).getDouble("longitude"));
                boolean free = false;
                int id = array.getJSONObject(i).getInt("chargingStationId");
                int slots = array.getJSONObject(i).getInt("slots");
                int slotsOccupied = array.getJSONObject(i).getInt("slotsOccupied");
                if (slots > slotsOccupied)
                    free = true;
                station = new Station(id, pos, free, slots, slotsOccupied);
                stations.add(station);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mCallback.onSuccess();
    }

    /**
     * @param array
     * @param callback
     */
    public void saveCars(JSONArray array, Customer.DataCallback callback) {
        allCars.clear();
        cars.clear();
        final Customer.DataCallback mCallback = callback;
        for (int i = 0; i < array.length(); i++) {
            Car car;
            try {
                boolean available = (array.getJSONObject(i).getInt("bookingState") == 1);

                int id = array.getJSONObject(i).getInt("carId");
                LatLng pos = new LatLng(array.getJSONObject(i).getDouble("lastKnownPositionLatitude"), array.getJSONObject(i).getDouble("lastKnownPositionLongitude"));
                boolean full = (array.getJSONObject(i).getInt("chargingState") == 3);

                int chargingLevel = array.getJSONObject(i).getInt("chargeLevel");
                String name = "Auto";
                car = new Car(id, pos, full, available, chargingLevel, name);
                car.setPlate(array.getJSONObject(i).getString("licensePlate"));
                Log.d("ADD", "allCars");
                allCars.add(car);
                if (available) {
                    Log.d("ADD", "cars");
                    cars.add(car);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mCallback.onSuccess();
    }
}

