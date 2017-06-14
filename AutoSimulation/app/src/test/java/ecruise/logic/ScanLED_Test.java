package ecruise.logic;

import ecruise.data.*;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * @author Tom
 * @since 27.04.2017.
 */
public class ScanLED_Test
{
    private boolean checkID = true;
    private BookingState bookingState = BookingState.BLOCKED;
    private ChargingState chargingState = ChargingState.FULL;

    public IServerConnection testServer = new IServerConnection()
    {
        @Override
        public boolean checkID(String chipCardUid)
        {
            return checkID;
        }

        @Override
        public boolean checkIDExists(String chipCardUid)
        {
            return false;
        }

        @Override
        public BookingState getBookingState()
        {
            return bookingState;
        }

        @Override
        public ChargingState getChargingState()
        {
            return chargingState;
        }

        @Override
        public boolean startTrip(String chipCardUid)
        {
            return false;
        }

        @Override
        public void endTrip(int distanceTravelled, int endCharingStationId)
        {

        }

    };

    public IScanDevice testNFCReader = new IScanDevice()
    {
        @Override
        public String scanChipCardUid()
        {
            return "";
        }
    };

    ScanLED scanLED = new ScanLED(testNFCReader);

    @Before
    public void setUp() throws Exception
    {
        Server.setConnection(testServer);
    }

    @Test
    public void charging_booked_me() throws Exception
    {
        checkID = true;
        chargingState = ChargingState.CHARGING;
        bookingState = BookingState.BOOKED;
        assertEquals(ColorCode.YELLOW, scanLED.calculateColorCode());
    }

    @Test
    public void charging_booked_other() throws Exception
    {
        checkID = false;
        chargingState = ChargingState.CHARGING;
        bookingState = BookingState.BOOKED;
        assertEquals(ColorCode.RED, scanLED.calculateColorCode());
    }

    @Test
    public void charging_available() throws Exception
    {
        chargingState = ChargingState.CHARGING;
        bookingState = BookingState.AVAILABLE;
        assertEquals(ColorCode.YELLOW, scanLED.calculateColorCode());
    }

    @Test
    public void full_booked_me() throws Exception
    {
        checkID = true;
        chargingState = ChargingState.FULL;
        bookingState = BookingState.BOOKED;
        assertEquals(ColorCode.GREEN, scanLED.calculateColorCode());
    }

    @Test
    public void full_booked_other() throws Exception
    {
        checkID = false;
        chargingState = ChargingState.FULL;
        bookingState = BookingState.BOOKED;
        assertEquals(ColorCode.RED, scanLED.calculateColorCode());
    }

    @Test
    public void full_available() throws Exception
    {
        chargingState = ChargingState.FULL;
        bookingState = BookingState.AVAILABLE;
        assertEquals(ColorCode.GREEN, scanLED.calculateColorCode());
    }

    @Test
    public void blocked() throws Exception
    {
        bookingState = BookingState.BLOCKED;
        assertEquals(ColorCode.RED, scanLED.calculateColorCode());
    }
}