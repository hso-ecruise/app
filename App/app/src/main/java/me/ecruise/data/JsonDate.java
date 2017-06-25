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
import java.util.TimeZone;

public class JsonDate
{
    private String jsonDateString;
    private Calendar calendar;

    public JsonDate(String jsonDateString) throws ParseException
    {
        this.jsonDateString = jsonDateString;
        calendar = GregorianCalendar.getInstance();
        String s = jsonDateString.replace("Z", "+00:00");
        try
        {
            s = s.substring(0, 22) + s.substring(23);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new ParseException("Invalid length", 0);
        }
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Log.d("TimeZone" , tz.getDisplayName());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        Date date = df.parse(s);
        calendar.setTime(date);
    }

    public JsonDate(Calendar calendar)
    {
        Date date = calendar.getTime();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Log.d("TimeZone" , tz.getDisplayName());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        String formatted = df.format(date);
        jsonDateString = formatted.substring(0, 22) + ":" + formatted.substring(22);
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
