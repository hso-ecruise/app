package me.ecruise.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button mDataButton = (Button) findViewById(R.id.dataButton);
        mDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAccountManagement();
            }
        });

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
}
