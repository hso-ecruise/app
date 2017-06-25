package me.ecruise.data;

/**
 * @author Tom
 * @since 30.05.2017.
 */

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class JsonDate
{
    private String jsonDateString;
    private Calendar calendar;

    public JsonDate(String jsonDateString) throws ParseException
    {
        this.jsonDateString = jsonDateString;
        calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = df.parse(jsonDateString);
        calendar.setTime(date);
    }

    public JsonDate(Calendar calendar)
    {
        this.calendar = calendar;
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String formatted = df.format(date);
        jsonDateString = formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    public String getDisplayString()
    {
        Log.d("DATE_TIME_STRING", this.getString());
        Calendar dateTime = this.getCalendar();

        dateTime.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        Date date = dateTime.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String displayString = df.format(date);
        return displayString;
    }

    public Calendar getCalendar()
    {
        return calendar;
    }

    public String getString()
    {
        return jsonDateString;
    }
}
