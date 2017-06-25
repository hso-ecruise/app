package ecruise.data;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import ecruise.logic.Logger;

import java.util.NoSuchElementException;

/**
 * Created by Tom on 21.03.2017.
 */
public class NFCReader implements IScanDevice
{
    private NfcAdapter nfcAdapter;
    private Intent intent;

    public NFCReader(Context context) throws NoSuchElementException
    {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null)
        {
            throw new NoSuchElementException("No NFC-Reader found on this device");
        }
        if (!nfcAdapter.isEnabled())
        {
            throw new NoSuchElementException("No NFC enabled on this device");
        }
    }

    @Override
    public String scanChipCardUid()
    {
        if (intent == null)
            throw new IllegalStateException("Check isReady() before calling this method");

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        return readTag(tag);
    }

    private String readTag(Tag tag)
    {
        String chipCardUid = "";
        try
        {
            byte[] tagId = tag.getId();

            StringBuilder sb = new StringBuilder();
            for (byte b : tagId)
            {
                sb.append(String.format("%02X", b));
            }

            chipCardUid = sb.toString();
        }
        catch (Exception e)
        {
            Logger.getInstance().log(e.getMessage());
        }

        intent = null;
        return chipCardUid;
    }

    public boolean onResume(Activity activity)
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
        return false;
    }

    public boolean onPause(Activity activity)
    {
        nfcAdapter.disableForegroundDispatch(activity);
        return false;
    }

    // this method checks if the the device is now going to scan the chip
    // and it will store the intent, which is needed for scanning
    public boolean isReady(Intent intent)
    {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            this.intent = intent;
            return true;
        }
        return false;
    }
}
