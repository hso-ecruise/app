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
            case GEBUCHT:
                if (Server.getInstance().checkID(scanDevice.scanUserId(), this.ID))
                {
                    return ColorCode.GREEN;
                }
                else
                {
                    return ColorCode.RED;
                }
            case LADEN_DANACH_GEBUCHT:
                if (Server.getInstance().checkID(scanDevice.scanUserId(), this.ID))
                {
                    return ColorCode.YELLOW;
                }
                else
                {
                    return ColorCode.RED;
                }
            case LADEN_DANACH_VERFUGBAR:
                return ColorCode.YELLOW;
            case VERFUGBAR:
                return ColorCode.GREEN;
            case NICHT_VERFUGBAR:
                return ColorCode.RED;
        }

        return ColorCode.YELLOW;
    }
}
