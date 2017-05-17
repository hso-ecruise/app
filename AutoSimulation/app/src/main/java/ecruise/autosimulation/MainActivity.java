package ecruise.autosimulation;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ScrollView;
import android.widget.TextView;
import ecruise.data.NFCReader;
import ecruise.data.Server;
import ecruise.data.ServerConnection;
import ecruise.logic.*;

public class MainActivity extends AppCompatActivity
{
    private ScanLED scanLED;
    private StatusLED statusLED;
    private NFCReader nfcReader;

    public MainActivity()
    {
        Logger.getInstance().addListener(new ILogListener()
        {
            @Override
            public void log(String text)
            {
                TextView log = (TextView) findViewById(R.id.log);
                log.append("\n" + android.text.format.DateFormat.format("HH:mm:ss: ", new java.util.Date()) + text);

                final ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                scroll.post(new Runnable()
                {
                    public void run()
                    {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
        Server.setConnection(new ServerConnection());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcReader = new NFCReader(getApplicationContext());
        scanLED = new ScanLED(nfcReader);
        statusLED = new StatusLED();

        setStatusColorCode(statusLED.calculateColorCode());

        /*final Handler handler = new Handler();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {

                    }
                }, 100);
            }
        }).start();*/
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        nfcReader.onResume(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        nfcReader.onPause(this);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        if (nfcReader.isReady(intent))
        {
            ColorCode result = scanLED.calculateColorCode();
            blinkScanLED(colorsFromColorCode(result));
            setInfoText("RFID-Card gescannt");

            if (result == ColorCode.GREEN)
            {
                setStatusColorCode(ColorCode.OFF);
                setStatusText(getResources().getString(R.string.discarging));
            }

        }
    }

    private int colorsFromColorCode(ColorCode colorCode)
    {
        switch (colorCode)
        {
            case RED:
                return Color.parseColor(getResources().getString(R.color.codeRed));
            case YELLOW:
                return Color.parseColor(getResources().getString(R.color.codeYellow));
            case GREEN:
                return Color.parseColor(getResources().getString(R.color.codeGreen));
            case BLUE:
                return Color.parseColor(getResources().getString(R.color.codeBlue));
            case OFF:
                return Color.parseColor(getResources().getString(R.color.codeOff));
        }
        return 0;
    }

    private void setInfoText(String infoText)
    {
        TextView textView = (TextView) findViewById(R.id.infoText);
        textView.setText(infoText);
    }

    private void setStatusText(String statusText)
    {
        TextView textView = (TextView) findViewById(R.id.statusText);
        textView.setText(statusText);
    }

    private void setStatusColorCode(ColorCode state)
    {
        setStatusLEDColor(colorsFromColorCode(state));
        switch (state)
        {
            case RED:
                setStatusText(getResources().getString(R.string.not_available));
                break;
            case YELLOW:
                setStatusText(getResources().getString(R.string.not_available));
                break;
            case GREEN:
                setStatusText(getResources().getString(R.string.later_available));
                break;
            case BLUE:
                setStatusText(getResources().getString(R.string.booked));
                break;
            case OFF:
                setStatusText(getResources().getString(R.string.diconnected));
                break;
        }
    }

    private void setStatusLEDColor(int color)
    {
        View led = findViewById(R.id.statusLed);
        led.setBackgroundColor(color);
    }

    private void blinkScanLED(int color)
    {
        final View led = findViewById(R.id.scanLed);
        GradientDrawable gradientDrawable = (GradientDrawable) led.getBackground();
        gradientDrawable.setColor(color);

        led.setAlpha(1.0f);
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            public void onAnimationEnd(Animation animation)
            {
                led.setAlpha(0.0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }

        });
        anim.setRepeatCount(6);
        anim.setDuration(100);
        anim.setStartOffset(20);
        led.startAnimation(anim);
    }
}
