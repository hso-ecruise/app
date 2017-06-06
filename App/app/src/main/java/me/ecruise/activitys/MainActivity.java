package me.ecruise.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.ecruise.data.Booking;
import me.ecruise.data.Customer;
import me.ecruise.data.Server;

public class MainActivity extends AppCompatActivity implements OnMenuItemClickListener{

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

    private void startAccountManagement(){
        Intent intent = new Intent(this, AccountManagementActivity.class);
        startActivity(intent);
    }
    private void startMap(){
        Intent intent = new Intent(this, Map2Activity.class);
        startActivity(intent);
    }
    private void startNewBooking(){
        Intent intent = new Intent(this, BookingActivity.class);
        startActivity(intent);
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_menu, popup.getMenu());

        popup.show();
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.data:
                startAccountManagement();
                return true;
            case R.id.logout:
                startAccountManagement();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getBookingsFromServer()
    {
        String url = "https://api.ecruise.me/v1/bookings/by-customer/1";// + Customer.getInstance(this.getApplicationContext()).getId();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        showBookings(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        Server.getInstance(this.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    public void showBookings(JSONArray bookings)
    {
        LinearLayout ll = (LinearLayout)findViewById(R.id.bookings);
        LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < bookings.length(); i++)
        {
            Booking bookingButton = null;
            try {
                bookingButton = new Booking(this.getApplicationContext(), bookings.getJSONObject(i).getInt("customerId"), 0, bookings.getJSONObject(i).getString("plannedDate"));
                ll.addView(bookingButton, lp);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }
}
