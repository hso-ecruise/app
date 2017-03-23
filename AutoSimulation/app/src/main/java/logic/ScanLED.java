package logic;

import data.IScanDevice;
import data.NFCReader;
import data.Server;

/**
 * Created by Tom on 21.03.2017.
 */
public class ScanLED
{
    private final int ID;
    private IScanDevice scanDevice;

    public ScanLED()
    {
        ID = 554;
        scanDevice = new NFCReader();
    }

    public ColorCode calculateColorCode()
    {
        switch (Server.getInstance().getCarState())
        {
            case BOOKED_FULL:
                if (Server.getInstance().checkID(scanDevice.scanUserId(), this.ID))
                {
                    return ColorCode.GREEN;
                }
                else
                {
                    return ColorCode.RED;
                }
            case BOOKED_CHARGING:
                if (Server.getInstance().checkID(scanDevice.scanUserId(), this.ID))
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
