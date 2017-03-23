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
            case BOOKED_FULL:
                return ColorCode.BLUE;
            case BOOKED_CHARGING:
                return ColorCode.BLUE;
            case AVAILABLE_CHARGING:
                return ColorCode.YELLOW;
            case AVAILABLE_FULL:
                return ColorCode.GREEN;
            case BLOCKED:
                return ColorCode.RED;
        }

        return ColorCode.OFF;
    }
}
