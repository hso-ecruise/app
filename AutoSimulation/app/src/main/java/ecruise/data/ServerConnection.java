package ecruise.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import ecruise.logic.JsonDate;
import ecruise.logic.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.toolbox.JsonArrayRequest;

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
    private static final String EMAIL = "";
    private static final String PASSWORD = "";

    private String token;

    private BookingState bookingState = null;
    private ChargingState chargingState = null;
    private String bookedChipCardUid = null; // if the car is booked, this will be the unique bookedChipCardUid of the customer
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

        // A copy of the relevant servers values is polled every 10 s
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    // the update methods do the polling asynchronously
                    updateBookingState();
                    updateChargingState();
                    updateChipCardUid();

                    try
                    {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
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
        String url = "https://api.ecruise.me/v1/public/login/" + EMAIL;
        String stringRequest = "\"" + PASSWORD + "\"";
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonStringRequest jsObjRequest = new JsonStringRequest
                (Request.Method.POST, url, stringRequest, future, future);

        addToRequestQueue(jsObjRequest);

        try
        {
            JSONObject response = future.get(5, TimeUnit.SECONDS); // this will block
            token = response.getString("token");
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
        if (chipCardUid == null)
            throw new IllegalStateException("This car is not booked");
        return chipCardUid.equals(this.bookedChipCardUid);
    }

    @Override
    public BookingState getBookingState()
    {
        return bookingState;
    }

    @Override
    public ChargingState getChargingState()
    {
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
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (InvalidParameterException e)
        {
            return false;
        }

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, trip, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", token);
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
                params.put("access_token", token);
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
                params.put("access_token", token);
                return params;
            }
        };

        addToRequestQueue(jsonArrayRequest);

        try
        {
            JSONArray customers = future.get(); // this will block

            for (int i = 0; i < customers.length(); i++)
            {
                JSONObject customer = customers.getJSONObject(i);
                if (customer.getString("bookedChipCardUid").equals(chipCardUid))
                {
                    return customer.getString("customerId");
                }
            }
        }
        catch (InterruptedException | JSONException | ExecutionException e)
        {
            e.printStackTrace();
        }
        throw new InvalidParameterException("No Customer with bookedChipCardUid " + chipCardUid + " found");
    }

    private void updateBookingState()
    {
        String url = "https://api.ecruise.me/v1/cars/" + CAR_ID;

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject car)
                    {
                        try
                        {
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
                                    Log.e("ServerConnection", "Unknown Bookingstate recieved from Server");
                                    break;
                            }
                            Log.d("ServerConnection", "updateBookingState to " + bookingState);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("ServerConnection", "Recieved " + error.getMessage() + ". Make sure the Car is present with the carId " + CAR_ID);
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", token);
                return params;
            }
        };
        addToRequestQueue(request);
    }

    private void updateChargingState()
    {
        String url = "https://api.ecruise.me/v1/cars/" + CAR_ID;

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject car)
                    {
                        try
                        {
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
                                    Log.e("ServerConnection", "Unknown ChargingState recieved from Server");
                                    break;
                            }
                            Log.d("ServerConnection", "updateBookingstate to " + chargingState);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("ServerConnection", "Recieved " + error.getMessage() + ". Make sure the Car is present with the carId " + CAR_ID);
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", token);
                return params;
            }
        };
        addToRequestQueue(request);
    }

    private void updateChipCardUid()
    {
        String url = "https://api.ecruise.me/v1//trips/by-car/" + CAR_ID;

        JsonArrayRequest request = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray trips)
                    {
                        try
                        {
                            for (int i = 0; i < trips.length(); i++)
                            {
                                JSONObject trip = null;
                                trip = trips.getJSONObject(i);

                                JsonDate startDate = new JsonDate(trip.getString("startDate"));

                                // trip is in future ("30 min booking")
                                if (startDate.getCalendar().after(Calendar.getInstance()))
                                {
                                    String customerId = trip.getString("customerId");

                                    String url = "https://api.ecruise.me/v1/customers/" + customerId;

                                    JsonObjectRequest innerRequest = new JsonObjectRequest
                                            (Request.Method.GET, url, null, new Response.Listener<JSONObject>()
                                            {
                                                @Override
                                                public void onResponse(JSONObject customer)
                                                {
                                                    try
                                                    {
                                                        bookedChipCardUid = customer.getString("bookedChipCardUid");
                                                        Log.d("ServerConnection", "updateChipCardUid to " + bookedChipCardUid);
                                                    }
                                                    catch (JSONException e)
                                                    {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, new Response.ErrorListener()
                                            {
                                                @Override
                                                public void onErrorResponse(VolleyError error)
                                                {
                                                    Log.e("ServerConnection", "Recieved " + error.getMessage());
                                                }
                                            })
                                    {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError
                                        {
                                            Map<String, String> params = new HashMap<>();
                                            params.put("access_token", token);
                                            return params;
                                        }
                                    };

                                    addToRequestQueue(innerRequest);

                                    // only one trip can match this condition
                                    break;
                                }
                                else
                                {
                                    bookedChipCardUid = null;
                                }
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("ServerConnection", "Recieved " + error.getMessage());
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", token);
                return params;
            }
        };
        addToRequestQueue(request);
    }
}
