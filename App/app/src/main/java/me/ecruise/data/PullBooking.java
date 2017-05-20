package me.ecruise.data;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import me.ecruise.activitys.R;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PullBooking extends Service
{
    private Timer timer = new Timer();

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
                String infoText;
                // getBuchungen
                infoText = "Nächste Buchung um 18:00 am 19.05.17";


                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(PullBooking.this)
                                .setSmallIcon(R.drawable.ic_play_light)
                                .setContentTitle("eCruise - Buchung")
                                .setContentText(infoText);
                int mNotificationId = new Random().nextInt();
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }
        },0, 30000 );
    }
}
