package me.ecruise.data;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Jens Ullrich on 30.05.2017.
 */

public class Booking extends Button {
    private int ID;
    private int carID;
    private Date bookingDate;
    private Date plannedDate;
    private String bookingDateString;
    private String plannedDateString;
    private LatLng bookedPos;
    private LatLng carPos;

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

    public Booking(Context context, int ID, int carID, String plannedDateString) {
        super(context);
        this.ID = ID;
        this.carID = carID;
        this.plannedDateString = plannedDateString;
        this.setText("Buchung Nr." + ID + "\n" + plannedDateString + "\n" + carID + "");
    }

}
