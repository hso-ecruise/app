package ecruise.autosimulation;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import ecruise.data.*;
import ecruise.logic.*;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    private ScanLED scanLED;
    private StatusLED statusLED;
    private IScanDevice nfcReader;

    private Timer statusTimer = new Timer();

    public Handler updateHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Log.d("MainActivity", "update Status");
            setStatusColorCode(statusLED.calculateColorCode());
        }
    };

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Server.setConnection(new ServerConnection(getApplicationContext()));

        try
        {
            nfcReader = new NFCReader(getApplicationContext());
        } catch (Exception e)
        {
            nfcReader = new DemoNFCReader();
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutFallback);
            linearLayout.setVisibility(View.VISIBLE);
            Button button = (Button) findViewById(R.id.buttonScanNfc);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    EditText editText = (EditText) findViewById(R.id.editTextNfcId);
                    String userId = editText.getText().toString();
                    ((DemoNFCReader) nfcReader).setUserID(userId);
                    ColorCode result = scanLED.calculateColorCode();
                    blinkScanLED(colorsFromColorCode(result));
                    setInfoText("RFID-Card gescannt");

                    if (result == ColorCode.GREEN)
                    {
                        setStatusColorCode(ColorCode.OFF);
                        setStatusText(getResources().getString(R.string.discarging));
                        ((DemoServerConnection) Server.getConnection()).SetDischarging();
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutFallback);
                        linearLayout.setVisibility(View.GONE);

                    }
                }
            });
        }


        scanLED = new ScanLED(nfcReader);
        statusLED = new StatusLED();


        statusTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                updateHandler.obtainMessage(1).sendToTarget();
            }
        }, 5000, 1000);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            ((NFCReader) nfcReader).onResume(this);
        } catch (ClassCastException e)
        {

        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        try
        {
            ((NFCReader) nfcReader).onPause(this);
        } catch (ClassCastException e)
        {

        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        try
        {
            if (((NFCReader) nfcReader).isReady(intent))
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
        } catch (ClassCastException e)
        {

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
                setStatusText(getResources().getString(R.string.later_available));
                break;
            case GREEN:
                setStatusText(getResources().getString(R.string.available));
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
