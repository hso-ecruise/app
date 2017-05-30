package me.ecruise.data;

/**
 * @author Tom
 * @since 30.05.2017.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
        calendar.setTime(date);
    }

    public JsonDate(Calendar calendar)
    {
        Date date = calendar.getTime();
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
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
