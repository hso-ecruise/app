package ecruise.logic;

import android.content.Context;
import ecruise.data.IScanDevice;
import ecruise.data.NFCReader;
import ecruise.data.Server;

/**
 * Created by Tom on 21.03.2017.
 */
public class ScanLED
{
    private final int carID = 1;
    private IScanDevice scanDevice;

    public ScanLED(IScanDevice scanDevice)
    {
        this.scanDevice = scanDevice;
    }

    public ColorCode calculateColorCode()
    {
        switch (new StatusLED().calculateCarState())
        {
            case BOOKED_FULL:
                if (Server.getConnection().checkID(scanDevice.scanUserId(), carID))
                {
                    return ColorCode.GREEN;
                }
                else
                {
                    return ColorCode.RED;
                }
            case BOOKED_CHARGING:
                if (Server.getConnection().checkID(scanDevice.scanUserId(), carID))
                {
                    return ColorCode.YELLOW;
                }
                else
                {
                    return ColorCode.RED;
                }
            case AVAILABLE_CHARGING:
                return ColorCode.YELLOW;
            case AVAILABLE_FULL:
                return ColorCode.GREEN;
            case BLOCKED:
                return ColorCode.RED;
        }

        return ColorCode.YELLOW;
    }
}
