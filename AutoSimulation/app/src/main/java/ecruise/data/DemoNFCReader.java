package ecruise.data;

/**
 * @author Tom
 * @since 04.06.2017.
 */
public class DemoNFCReader implements IScanDevice
{
    private String userID;

    @Override
    public String scanChipCardUid()
    {
        return userID;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }
}
