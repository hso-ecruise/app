package me.ecruise.data;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import me.ecruise.activitys.MainActivity;
import me.ecruise.activitys.R;

import java.util.*;

public class PullBooking extends Service
{
    private Timer timer = new Timer();

    private int recievedBooking = 0;

    public PullBooking()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        startTimer();
    }

    private void startTimer()
    {
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                if (Customer.getInstance(PullBooking.this).getName() != null)
                {
                    // getBuchungen()

                    int latestBookingID = 0;

                    if(recievedBooking != latestBookingID)
                    {
                        recievedBooking = latestBookingID;
                        String infoText = "Test";

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
                }
            }
        }, 0, 30000);
    }
}
