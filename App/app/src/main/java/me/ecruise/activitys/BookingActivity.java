package me.ecruise.activitys;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.EditText;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity {

    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button mMapButton = (Button) findViewById(R.id.manualPositionButton);
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMap();
            }
        });


        final EditText dateTextEdit = (EditText) findViewById(R.id.dateText);
        dateTextEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                Calendar mcurrentDate=Calendar.getInstance();
                mYear=mcurrentDate.get(Calendar.YEAR);
                mMonth=mcurrentDate.get(Calendar.MONTH);
                mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(BookingActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int year, int monthOfYear, int dayOfMonth) {
                        String date_selected = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
                        dateTextEdit.setText(date_selected);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Datum wählen");
                mDatePicker.show();  }
        });

        final EditText timeTextEdit = (EditText) findViewById(R.id.timeText);
        timeTextEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                Calendar mcurrentDate=Calendar.getInstance();
                mHour=mcurrentDate.get(Calendar.HOUR_OF_DAY);
                mMinute=mcurrentDate.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker=new TimePickerDialog(BookingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker timepicker, int hourOfDay, int minute) {
                        String time_selected = hourOfDay + ":" + minute;
                        timeTextEdit.setText(time_selected);
                    }
                }, mHour, mMinute, DateFormat.is24HourFormat(BookingActivity.this));
                mTimePicker.setTitle("Uhrzeit wählen");
                mTimePicker.show();  }
        });
    }
    private void startMap(){
        Intent intent = new Intent(this, Map2Activity.class);
        startActivity(intent);
    }
}

