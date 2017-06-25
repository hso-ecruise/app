package me.ecruise.activitys;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import me.ecruise.data.Booking;
import me.ecruise.data.Customer;
import me.ecruise.data.JsonDate;
import me.ecruise.data.Server;

public class BookingActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Calendar mPlannedDate;

    /**
     * Initializes the Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CheckBox mAutoGpsBox = (CheckBox) findViewById(R.id.autoGpsBox);

        Button mMapButton = (Button) findViewById(R.id.manualPositionButton);
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateSelectedDateAndTime())
                    startMap();
                else
                    dateTimeFailureAlert();
            }
        });

        Button mBookingButton = (Button) findViewById(R.id.bookButton);
        mBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button", "book");
                if (validateSelectedDateAndTime()) {
                    Log.d("Date", "valid");
                    if (mAutoGpsBox.isChecked()) {
                        Log.d("Box", "checked");
                        mGoogleApiClient.connect();
                    } else {
                        Log.d("Box", "not checked");
                        startMap();

                    }
                } else {
                    dateTimeFailureAlert();
                }

            }
        });
        mPlannedDate = Calendar.getInstance();
        //mPlannedDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        final EditText dateTextEdit = (EditText) findViewById(R.id.dateText);
        dateTextEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                Calendar mCurrentDate = Calendar.getInstance();
                int year = mCurrentDate.get(Calendar.YEAR);
                int month = mCurrentDate.get(Calendar.MONTH);
                int day = mCurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(BookingActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int year, int monthOfYear, int dayOfMonth) {
                        String date_selected = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
                        dateTextEdit.setText(date_selected);
                        mPlannedDate.set(year, monthOfYear, dayOfMonth);
                    }
                }, year, month, day);
                mDatePicker.setTitle("Datum wählen");
                mDatePicker.show();
            }
        });

        final EditText timeTextEdit = (EditText) findViewById(R.id.timeText);
        timeTextEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                Calendar mCurrentDate = Calendar.getInstance();
                int hour = mCurrentDate.get(Calendar.HOUR_OF_DAY);
                int minute = mCurrentDate.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker = new TimePickerDialog(BookingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker timepicker, int hourOfDay, int minute) {
                        String time_selected = hourOfDay + ":" + minute + " Uhr";
                        timeTextEdit.setText(time_selected);
                        mPlannedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mPlannedDate.set(Calendar.MINUTE, minute);
                    }
                }, hour, minute, DateFormat.is24HourFormat(BookingActivity.this));
                mTimePicker.setTitle("Uhrzeit wählen");
                mTimePicker.show();
            }
        });

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                postBooking(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
            }
        }
    }

    /**
     * Validates the selected Date and Time
     * @return true if the date is at least 30min in the future
     */
    private boolean validateSelectedDateAndTime() {
        Calendar testTime = Calendar.getInstance();
        testTime.add(Calendar.MINUTE, 30);
        if (mPlannedDate.after(testTime))
            return true;
        return false;
    }

    /**
     * Starts a new Map Activity where the location can be chosen
     */
    private void startMap() {
        me.ecruise.data.Map.getInstance(this.getApplicationContext()).setShowAllCars(false);
        me.ecruise.data.Map.getInstance(this.getApplicationContext()).setShowBookedCar(false);
        me.ecruise.data.Map.getInstance(this.getApplicationContext()).setShowStations(false);
        me.ecruise.data.Map.getInstance(this.getApplicationContext()).setGetLocation(true);
        Intent intent = new Intent(BookingActivity.this, Map2Activity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Google", "connected");
        autoPosBooking();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Creates an info-alert
     */
    private void successAlert() {
        Log.d("Alert", "Success");
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Buchung erfolgreich");
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    /**
     * Creates a failure Alert
     */
    private void failureAlert() {
        Log.d("Alert", "Failure");
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Fehler bei der Buchung");
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

    /**
     * Creates a failure Alert
     */
    private void dateTimeFailureAlert() {
        Log.d("Alert", "Failure");
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Ungültiger Zeitpunkt!");
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

    /**
     * determines the position for the booking with the location of the smartphone
     */
    private void autoPosBooking() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            Log.d("Booking", "post");
            postBooking(latitude, longitude);
        } else {
            Log.d("Fail", "no lastLocation");
            failureAlert();
        }
    }

    /**
     * posts a booking to the server
     * @param lat latitude of the booked position
     * @param lng longitude of the booked position
     */
    public void postBooking(double lat, double lng) {
        final String mToken = Customer.getInstance(this.getApplicationContext()).getToken();
        Log.d("Post", "booking");
        int id = Customer.getInstance(this.getApplicationContext()).getId();

        JsonDate plannedDate = new JsonDate(mPlannedDate);

        String plannedDateString = plannedDate.getString();
        Log.d("Planned Time", plannedDate.getString());
        String url = "https://api.ecruise.me/v1/bookings/";
        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put("customerId", id);
            reqObj.put("bookingPositionLatitude", lat);
            reqObj.put("bookingPositionLongitude", lng);
            reqObj.put("plannedDate", plannedDateString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("reqObj", reqObj.toString());
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, reqObj, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        successAlert();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        failureAlert();

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", mToken);
                return params;
            }
        };
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        String message = "Jetzt verbindlich Buchen?";
        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("Ja",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                        Server.getInstance(null).addToRequestQueue(jsObjRequest);
                    }
                });
        dlgAlert.setNegativeButton("Nein",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.create().show();
    }
}

