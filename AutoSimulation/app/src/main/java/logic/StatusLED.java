package logic;

import data.Server;

/**
 * Created by Tom on 21.03.2017.
 */
public class StatusLED
{
    public ColorCode calculateColorCode()
    {
        CarState state = Server.getInstance().getCarState();

        switch (state)
        {
            case GEBUCHT:
                return ColorCode.BLUE;
            case LADEN_DANACH_GEBUCHT:
                return ColorCode.BLUE;
            case LADEN_DANACH_VERFUGBAR:
                return ColorCode.YELLOW;
            case VERFUGBAR:
                return ColorCode.GREEN;
            case NICHT_VERFUGBAR:
                return ColorCode.RED;
        }

        return ColorCode.OFF;
    }
}
