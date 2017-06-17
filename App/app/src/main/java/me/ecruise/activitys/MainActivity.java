package me.ecruise.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.ecruise.data.Booking;
import me.ecruise.data.Customer;
import me.ecruise.data.Server;

public class MainActivity extends AppCompatActivity implements OnMenuItemClickListener {

    /**
     * initializes the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Button mMapButton = (Button) findViewById(R.id.mapButton);
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMap();
            }
        });

        Button mNewBookingButton = (Button) findViewById(R.id.newBookingButton);
        mNewBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewBooking();
            }
        });

        getBookingsFromServer();
    }

    /**
     * starts a new AccountManagementActivity
     */
    private void startAccountManagement() {
        Intent intent = new Intent(this, AccountManagementActivity.class);
        startActivity(intent);
    }

    /**
     * starts a new standard MapActivity
     */
    private void startMap() {
        Intent intent = new Intent(this, Map2Activity.class);
        me.ecruise.data.Map.getInstance(this.getApplicationContext()).setShowStations(true);
        me.ecruise.data.Map.getInstance(this.getApplicationContext()).setShowAllCars(true);
        startActivity(intent);
    }

    /**
     * starts a new MapActivity which shows the booked car
     * @param booking
     */
    private void startBookedCarMap(Booking booking) {

        Intent intent = new Intent(this, Map2Activity.class);
        me.ecruise.data.Map.getInstance(this.getApplicationContext()).setShowBookedCar(true);
        startActivity(intent);
    }

    /**
     * starts a new bookingActivity
     */
    private void startNewBooking() {
        Intent intent = new Intent(this, BookingActivity.class);
        startActivity(intent);
    }

    /**
     * shows a popup
     * @param v
     */
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_menu, popup.getMenu());

        popup.show();
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.data:
                startAccountManagement();
                return true;
            case R.id.notifications:
                return true;
            case R.id.logout:
                logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * logs the user out
     */
    public void logout() {
        Log.d("Logout", "Main");
        SharedPreferences savedLogin =  PreferenceManager.getDefaultSharedPreferences((this.getApplicationContext()));
        SharedPreferences.Editor editor = savedLogin.edit();
        editor.putInt("userId", 0);
        editor.putString("token", "");
        // Commit the edits!
        editor.commit();
        Intent intent = new Intent(this , LoginActivity.class);
        startActivity(intent);
    }

    /**
     * gets bookings from the server
     */
    public void getBookingsFromServer() {
        final String mToken = Customer.getInstance(this.getApplicationContext()).getToken();
        String url = "https://api.ecruise.me/v1/bookings/by-customer/" + Customer.getInstance(this.getApplicationContext()).getId();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        showBookings(response);
                        Log.d("booking: ", response.toString());
                    }


                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }


                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", mToken);
                return params;
            }
        };
        Server.getInstance(this.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    /**
     * shows all bookings in a linear layout
     * @param bookings
     */
    public void showBookings(JSONArray bookings) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.bookings);
        LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < bookings.length(); i++) {
            final Booking bookingButton;
            try {
                bookingButton = new Booking(this.getApplicationContext(), bookings.getJSONObject(i).getInt("bookingId"), 0, bookings.getJSONObject(i).getString("plannedDate"));
                ll.addView(bookingButton, lp);
                bookingButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        startBookedCarMap(bookingButton);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }
}
