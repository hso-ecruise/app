package ecruise.data;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import ecruise.logic.Logger;

/**
 * Created by Tom on 21.03.2017.
 */
public class NFCReader implements IScanDevice
{
    private NfcAdapter nfcAdapter;
    private Intent intent;

    public NFCReader(Context context)
    {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null)
        {
            Logger.getInstance().log("Dieses Gerät unterstützt kein NFC");
        }
        if (!nfcAdapter.isEnabled())
        {
            Logger.getInstance().log("Auf diesem Gerät ist NFC ausgeschaltet");
        }
    }

    @Override
    public String scanUserId()
    {
        if (intent == null)
            throw new IllegalStateException("Check isReady() before calling this method");

        String chipCardUid = "";
        try
        {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            byte[] tagId = tag.getId();

            StringBuilder sb = new StringBuilder();
            for (byte b : tagId)
            {
                sb.append(String.format("%02X", b));
                sb.append(':');
            }
            sb.setLength(sb.length() - 1);

            chipCardUid = sb.toString();
            Logger.getInstance().log("TagID: " + chipCardUid);
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
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
        return false;
    }

    public boolean onPause(Activity activity)
    {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        nfcAdapter.disableForegroundDispatch(activity);
        return false;
    }

    public boolean isReady(Intent intent)
    {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            this.intent = intent;
            return true;
        }
        return false;
    }
}
