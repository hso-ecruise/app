package me.ecruise.data;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.SparseIntArray;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import me.ecruise.activitys.MainActivity;
import me.ecruise.activitys.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map;

public class PullBooking extends Service {
    private Timer timer = new Timer();

    private SparseIntArray recievedBookings = new SparseIntArray();

    public PullBooking() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Backgroundservice", "Started");

        startTimer();
    }

    private void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Customer.getInstance(null) != null) {
                    checkNewTripAssigned();
                }
            }
        }, 0, 30000);
    }

    private void showNotification() {
        String infoText = "Zu Ihrer Buchung wurde ein Auto zugeteilt";

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(PullBooking.this)
                        .setSmallIcon(R.drawable.ic_play_light)
                        .setContentTitle("eCruise - Buchung")
                        .setContentText(infoText)
                        .setContentIntent(PendingIntent.getActivity(PullBooking.this, 0,
                                new Intent(PullBooking.this, MainActivity.class), 0));
        int mNotificationId = new Random().nextInt();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void checkNewTripAssigned() {
        Log.d("Backgroundservice", "Check new bookings for CustomerId: " + Customer.getInstance(null).getId());

        String url = "https://api.ecruise.me/v1/bookings/by-customer/" + Customer.getInstance(null).getId();


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray bookings) {
                        try {
                            Boolean isNewTripAssigned = false;

                            for (int i = 0; i < bookings.length(); i++) {
                                JSONObject booking = null;

                                booking = bookings.getJSONObject(i);

                                int bookingId = booking.getInt("bookingId");
                                int tripId = booking.getInt("tripId");
                                String plannedDate = booking.getString("plannedDate");

                                JsonDate date = null;
                                try {
                                    date = new JsonDate(plannedDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                // only if trip is in future
                                if (date.getCalendar().after(Calendar.getInstance())) {
                                    // the whole booking is new
                                    if (recievedBookings.get(bookingId, -1) == -1) {
                                        Log.d("Backgroundservice", "New Booking with ID " + bookingId);

                                        if (tripId != 0)
                                            isNewTripAssigned = true;

                                        recievedBookings.append(bookingId, tripId);
                                    } else {
                                        // the trip id has changed since last check
                                        if (recievedBookings.get(bookingId) != tripId) {
                                            isNewTripAssigned = true;
                                        }
                                    }
                                }
                            }

                            if (Customer.getInstance(null) != null && isNewTripAssigned) {
                                showNotification();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", Customer.getInstance(null).getToken());
                return params;
            }
        };


        Server.getInstance(this.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }
}