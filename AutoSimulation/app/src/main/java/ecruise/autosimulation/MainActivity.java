package ecruise.autosimulation;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import ecruise.data.NFCReader;
import ecruise.data.OnFinishedHandler;
import ecruise.data.Server;
import ecruise.data.ServerConnection;
import ecruise.logic.Car;
import ecruise.logic.ColorCode;
import ecruise.logic.Logger;

import java.security.InvalidParameterException;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    private Car car;
    private NFCReader nfcReader;

    private Timer statusTimer = new Timer();

    // polls the color of the status LED and updates Position if necessary
    public Handler updateHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Log.d("MainActivity", "update Status");
            car.getStatusLedColorCode((colorCode) ->
            {
                setStatus(colorCode);
            });
            car.updatePositionToPausingPosition((success) ->
            {
            });
        }
    };

    public MainActivity()
    {
        // output of the logger goes to a textview in this activity
        Logger.getInstance().addListener(text -> runOnUiThread(() ->
        {
            TextView log = (TextView) findViewById(R.id.log);
            // new line should not appear first time
            if (log.getText().length() > 0)
            {
                log.append("\n");
            }
            log.append(android.text.format.DateFormat.format("HH:mm:ss: ", new java.util.Date()) + text);

            final ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
            boolean autoScroll = ((CheckBox) findViewById(R.id.checkBoxAutoscroll)).isChecked();
            if (autoScroll)
            {
                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            }
            Log.d("LOGGER", text);
        }));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertAskForCarId((carId) ->
        {
            if (carId == null)
            {
                setStatus(ColorCode.RED);
                return;
            }

            Server.setConnection(new ServerConnection(getApplicationContext(), (success) ->
            {
                if (success)
                {
                    try
                    {
                        nfcReader = new NFCReader(getApplicationContext());
                        car = new Car(carId, nfcReader);

                        statusTimer.scheduleAtFixedRate(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                updateHandler.obtainMessage(1).sendToTarget();
                            }
                        }, 100, 10000);

                        setInfoText(getResources().getString(R.string.prompt_info));
                    }
                    catch (NoSuchElementException | InvalidParameterException e)
                    {
                        setStatus(ColorCode.RED);
                        return;
                    }
                }
                else
                {
                    setStatus(ColorCode.RED);
                    return;
                }
            }));
        });


        // during a trip to end the trip
        final Button buttonEndTrip = (Button) findViewById(R.id.buttonEndTrip);
        buttonEndTrip.setOnClickListener(v ->
        {
            car.endTrip((success) ->
            {
                if (success)
                {
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutTrip);
                    linearLayout.setVisibility(View.GONE);
                    setInfoText(getResources().getString(R.string.prompt_info));

                    // If trip ended successfully update state immediately
                    updateHandler.obtainMessage(1).sendToTarget();
                }
            });

        });

        // during a trip to pause the trip
        final Button buttonPause = (Button) findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(v ->
        {
            car.pause();
            buttonPause.setEnabled(false);
            buttonEndTrip.setEnabled(false);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutTrip);
            linearLayout.setVisibility(View.GONE);
            setInfoText(getResources().getString(R.string.prompt_info));
        });
    }

    private void alertAskForCarId(OnFinishedHandler<Integer> onFinishedHandler)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Setup");
        alertDialog.setMessage("Welches Auto soll für die Simulation verwendet werden? Geben Sie bitte die Auto-ID ein.");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Verbinden",
                (dialog, which) ->
                {
                    try
                    {
                        int carId = Integer.parseInt(input.getText().toString());
                        onFinishedHandler.handle(carId);
                    }
                    catch (NumberFormatException e)
                    {
                        onFinishedHandler.handle(null);
                    }
                });

        alertDialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        if (nfcReader.isReady(intent))
        {
            handleScan((colorCode) ->
            {
                // If scanned update state immediately
                updateHandler.obtainMessage(1).sendToTarget();
            });
        }
    }

    private void handleScan(OnFinishedHandler<ColorCode> onFinishedHandler)
    {
        car.scanNfc((colorCode) ->
        {
            if (colorCode == null)
            {
                blinkScanLED(colorsFromColorCode(ColorCode.RED));
                onFinishedHandler.handle(null);
                return;
            }

            if (colorCode != ColorCode.OFF)
            {
                blinkScanLED(colorsFromColorCode(colorCode));
            }

            if (colorCode == ColorCode.GREEN)
            {
                setStatus(ColorCode.OFF);
                setStatusText(getResources().getString(R.string.driving));
                setInfoText(getResources().getString(R.string.while_driving_info));
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutTrip);
                linearLayout.setVisibility(View.VISIBLE);
                findViewById(R.id.buttonPause).setEnabled(true);
                findViewById(R.id.buttonEndTrip).setEnabled(true);
            }
            onFinishedHandler.handle(colorCode);
        });
    }

    private int colorsFromColorCode(ColorCode colorCode)
    {
        if (colorCode == null)
        {
            return getColorFromResource(R.color.codeOff);
        }

        switch (colorCode)
        {
            case RED:
                return getColorFromResource(R.color.codeRed);
            case YELLOW:
                return getColorFromResource(R.color.codeYellow);
            case GREEN:
                return getColorFromResource(R.color.codeGreen);
            case BLUE:
                return getColorFromResource(R.color.codeBlue);
            case OFF:
                return getColorFromResource(R.color.codeOff);
        }
        return getColorFromResource(R.color.codeError);
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

    private void setStatus(ColorCode state)
    {
        setStatusLEDColor(colorsFromColorCode(state));

        if (state == null)
        {
            setStatusText(getResources().getString(R.string.disconnected));
            return;
        }

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
                setStatusText(getResources().getString(R.string.disconnected));
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
                findViewById(R.id.infoText).setVisibility(View.GONE);
                findViewById(R.id.scanInfoText).setVisibility(View.VISIBLE);
            }

            public void onAnimationEnd(Animation animation)
            {
                led.setAlpha(0.0f);
                findViewById(R.id.infoText).setVisibility(View.VISIBLE);
                findViewById(R.id.scanInfoText).setVisibility(View.GONE);
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

    private int getColorFromResource(int id)
    {
        return ContextCompat.getColor(getApplicationContext(), id);
    }
}
