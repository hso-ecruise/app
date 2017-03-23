package data;

/**
 * Created by Tom on 21.03.2017.
 */
public class NFCReader implements IScanDevice
{
    @Override
    public int scanUserId()
    {
        try
        {
            Thread.sleep(6000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return 0;
    }
}
