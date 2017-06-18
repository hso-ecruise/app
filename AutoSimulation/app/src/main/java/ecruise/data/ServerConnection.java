package ecruise.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import ecruise.logic.JsonDate;
import ecruise.logic.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created by Tom on 28.03.2017.
 */
// the interface to the api.ecruise.me
public class ServerConnection implements IServerConnection
{
    private static final String CAR_ID = "0";
    private static final String AUTH_EMAIL = "";
    private static final String AUTH_PASSWORD = "";
    private String accessToken;

    private BookingState bookingState = null;
    private ChargingState chargingState = null;
    private String bookedChipCardUid = null;
    private int tripId = -1;

    private RequestQueue requestQueue;
    private static Context mCtx;

    public ServerConnection(Context ctx)
    {
        mCtx = ctx;
        requestQueue = getRequestQueue();
        if (!authenticate())
        {
            throw new InvalidParameterException("Credentials are invalid for api.ecruise.me");
        }
    }

    private RequestQueue getRequestQueue()
    {
        if (requestQueue == null)
        {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }

    private boolean authenticate()
    {
        String url = "https://api.ecruise.me/v1/public/login/" + AUTH_EMAIL;
        String stringRequest = "\"" + AUTH_PASSWORD + "\"";
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonStringRequest jsObjRequest = new JsonStringRequest
                (Request.Method.POST, url, stringRequest, future, future);

        addToRequestQueue(jsObjRequest);

        try
        {
            JSONObject response = future.get(5, TimeUnit.SECONDS); // this will block
            accessToken = response.getString("accessToken");
        }
        catch (ExecutionException | JSONException | InterruptedException | TimeoutException e1)
        {
            e1.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean checkID(String chipCardUid)
    {
        if (getBookingState() != BookingState.BOOKED)
            throw new IllegalStateException("This car is not booked");

        try
        {
            updateBookedChipCardUid();
        }
        catch (ExecutionException | ParseException | JSONException | InterruptedException e)
        {
            e.printStackTrace();
        }

        return chipCardUid.equals(this.bookedChipCardUid);
    }

    @Override
    public boolean checkIDExists(String chipCardUid)
    {
        try
        {
            getCustomerIdFromChipCardUid(chipCardUid);
        }
        catch (InvalidParameterException e)
        {
            return false;
        }
        catch (InterruptedException | ExecutionException | JSONException e)
        {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public BookingState getBookingState()
    {
        try
        {
            updateCarState();
        }
        catch (ExecutionException | JSONException | InterruptedException e)
        {
            e.printStackTrace();
        }
        return bookingState;
    }

    @Override
    public ChargingState getChargingState()
    {
        try
        {
            updateCarState();
        }
        catch (ExecutionException | JSONException | InterruptedException e)
        {
            e.printStackTrace();
        }
        return chargingState;
    }

    @Override
    public boolean startTrip(String chipCardUid)
    {
        String url = "https://api.ecruise.me/v1/trips";
        JSONObject trip = new JSONObject();
        try
        {
            trip.put("carId", CAR_ID);
            trip.put("customerId", getCustomerIdFromChipCardUid(chipCardUid));
            trip.put("startDate", new JsonDate(Calendar.getInstance()).getString());
        }
        catch (InvalidParameterException e)
        {
            // There is no customer with this ChipCardUid
            return false;
        }
        catch (InterruptedException | ExecutionException | JSONException e)
        {
            e.printStackTrace();
        }

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, trip, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", accessToken);
                return params;
            }
        };
        addToRequestQueue(jsonObjectRequest);

        try
        {
            JSONObject result = future.get(); // this will block

            tripId = result.getInt("id");
            Logger.getInstance().log("Trip created with ID " + tripId);
        }
        catch (InterruptedException | JSONException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return true;
    }


    @Override
    public void endTrip(int distanceTravelled, int endCharingStationId)
    {
        if (tripId == -1)
            throw new IllegalStateException("Trip has not started yet");
        String url = "https://api.ecruise.me/v1/trips/" + tripId;

        JSONObject patch = new JSONObject();
        try
        {
            patch.put("DistanceTravelled", distanceTravelled);
            patch.put("EndChargingStationId", endCharingStationId);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.PATCH, url, null, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", accessToken);
                return params;
            }
        };
        addToRequestQueue(jsonObjectRequest);

        try
        {
            JSONObject result = future.get(); // this will block

            tripId = result.getInt("id");
            Logger.getInstance().log("Trip ended with ID " + tripId);
            tripId = -1;
        }
        catch (InterruptedException | JSONException | ExecutionException e)
        {
            e.printStackTrace();
        }
    }

    private String getCustomerIdFromChipCardUid(String chipCardUid)
            throws ExecutionException, InterruptedException, JSONException, InvalidParameterException
    {
        String url = "https://api.ecruise.me/v1/customers/";

        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", accessToken);
                return params;
            }
        };

        addToRequestQueue(jsonArrayRequest);


        JSONArray customers = future.get(); // this will block

        for (int i = 0; i < customers.length(); i++)
        {
            JSONObject customer = customers.getJSONObject(i);
            if (customer.getString("chipCardUid").equals(chipCardUid))
            {
                return customer.getString("customerId");
            }
        }

        throw new InvalidParameterException("No Customer with bookedChipCardUid " + chipCardUid + " found");
    }

    private String getChipCardUidFromCustomerId(String customerId)
            throws JSONException, ExecutionException, InterruptedException
    {
        String url = "https://api.ecruise.me/v1/customers/" + customerId;

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", accessToken);
                return params;
            }
        };
        addToRequestQueue(jsonArrayRequest);

        JSONObject customer = future.get(); // this will block
        return customer.getString("chipCardUid");
    }

    private void updateCarState()
            throws ExecutionException, InterruptedException, JSONException
    {
        String url = "https://api.ecruise.me/v1/cars/" + CAR_ID;
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", accessToken);
                return params;
            }
        };
        addToRequestQueue(request);

        JSONObject car = future.get();

        switch (car.getString("chargingState"))
        {
            case "DISCHARGING":
                chargingState = ChargingState.DISCHARGING;
                break;
            case "CHARGING":
                chargingState = ChargingState.CHARGING;
                break;
            case "FULL":
                chargingState = ChargingState.FULL;
                break;
            default:
                Log.e("ServerConnection", "Unknown ChargingState received from Server");
                break;
        }
        Log.d("ServerConnection", "updateBookingstate to " + chargingState);

        switch (car.getString("bookingState"))
        {
            case "AVAILABLE":
                bookingState = BookingState.AVAILABLE;
                break;
            case "BOOKED":
                bookingState = BookingState.BOOKED;
                break;
            case "BLOCKED":
                bookingState = BookingState.BLOCKED;
                break;
            default:
                Log.e("ServerConnection", "Unknown BookingState received from Server");
                break;
        }
        Log.d("ServerConnection", "updateBookingState to " + bookingState);
    }

    private void updateBookedChipCardUid()
            throws ExecutionException, InterruptedException, JSONException, ParseException
    {
        JSONArray trips = getTrips();

        for (int i = 0; i < trips.length(); i++)
        {
            JSONObject trip = trips.getJSONObject(i);

            JsonDate startDate = new JsonDate(trip.getString("startDate"));

            // trip is in future ("30 min booking")
            if (startDate.getCalendar().after(Calendar.getInstance()))
            {
                String bookedCustomerId = trip.getString("customerId");
                bookedChipCardUid = getChipCardUidFromCustomerId(bookedCustomerId);
                break;
            }
        }
    }

    private JSONArray getTrips()
            throws ExecutionException, InterruptedException
    {
        String url = "https://api.ecruise.me/v1//trips/by-car/" + CAR_ID;

        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayRequest request = new JsonArrayRequest
                (Request.Method.GET, url, null, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", accessToken);
                return params;
            }
        };
        addToRequestQueue(request);

        return future.get();
    }

}
