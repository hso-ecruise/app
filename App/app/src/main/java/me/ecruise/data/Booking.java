package me.ecruise.data;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Jens Ullrich on 30.05.2017.
 */

public class Booking extends Button {
    private int ID;
    private int carID;
    private int tripID;
    private Date bookingDate;
    private Date plannedDate;
    private String bookingDateString;
    private String plannedDateString;
    private LatLng bookedPos;
    private LatLng carPos;

    public int getTripID() {
        return tripID;
    }

    public void setTripID(int tripID) {
        this.tripID = tripID;
    }
    public LatLng getCarPos() {
        return carPos;
    }

    public void setCarPos(LatLng carPos) {
        this.carPos = carPos;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getCarID() {
        return carID;
    }

    public void setCarID(int carID) {
        this.carID = carID;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Date getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(Date plannedDate) {
        this.plannedDate = plannedDate;
    }

    public LatLng getBookedPos() {
        return bookedPos;
    }

    public void setBookedPos(LatLng bookedPos) {
        this.bookedPos = bookedPos;
    }

    public Booking(Context context) {
        super(context);
    }

    /**
     *
     * @param context
     * @param ID
     * @param plannedDateString
     * @param bookedPos
     */
    public Booking(Context context, int ID, String plannedDateString, LatLng bookedPos) {
        super(context);
        this.ID = ID;
        this.plannedDateString = plannedDateString;
        this.setText("Buchung Nr." + ID + "\n" + plannedDateString);
        this.setBookedPos(bookedPos);
    }
}
