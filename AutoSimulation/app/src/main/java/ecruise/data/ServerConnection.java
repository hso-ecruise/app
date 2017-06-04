package ecruise.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import ecruise.logic.JsonDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * Created by Tom on 28.03.2017.
 */
public class ServerConnection implements IServerConnection
{
    private static final String CAR_ID = "0";
    BookingState bookingState = null;
    ChargingState chargingState = null;
    String chipCardUid = null;

    private RequestQueue mRequestQueue;
    private static Context mCtx;

    public ServerConnection(Context ctx)
    {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();


        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
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
        if (mRequestQueue == null)
        {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }

    @Override
    public boolean checkID(String chipCardUid)
    {
        if (chipCardUid == null)
            throw new IllegalStateException("This car is not booked");
        return chipCardUid.equals(this.chipCardUid);
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
                });
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
                });
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
                                                        chipCardUid = customer.getString("chipCardUid");
                                                        Log.d("ServerConnection", "updateChipCardUid to " + chipCardUid);
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
                                            });

                                    addToRequestQueue(innerRequest);

                                    // only one trip can match this condition
                                    break;
                                }
                                else
                                {
                                    chipCardUid = null;
                                }
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } catch (ParseException e)
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
                });
        addToRequestQueue(request);
    }
}
