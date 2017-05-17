package ecruise.logic;

import ecruise.data.BookingState;
import ecruise.data.ChargingState;
import ecruise.data.IServerConnection;
import ecruise.data.Server;
import ecruise.logic.ColorCode;
import ecruise.logic.StatusLED;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyStore;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class StatusLED_Test
{
    private boolean checkID = true;
    private BookingState bookingState = BookingState.BLOCKED;
    private ChargingState chargingState = ChargingState.FULL;

    public IServerConnection testServer = new IServerConnection()
    {
        @Override
        public boolean checkID(int UserID, int tripID)
        {
            return checkID;
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


    };

    StatusLED statusLED = new StatusLED();

    @Before
    public void initialize()
    {
        Server.setConnection(testServer);
    }

    @Test
    public void booked_charging_test() throws Exception
    {
        bookingState = BookingState.BOOKED;
        chargingState = ChargingState.CHARGING;

        assertEquals(ColorCode.BLUE, statusLED.calculateColorCode());
    }

    @Test
    public void booked_full_test() throws Exception
    {
        bookingState = BookingState.BOOKED;
        chargingState = ChargingState.FULL;

        assertEquals(ColorCode.BLUE, statusLED.calculateColorCode());
    }

    @Test
    public void booked_discharging_test() throws Exception
    {
        bookingState = BookingState.BOOKED;
        chargingState = ChargingState.DISCHARGING;

        assertEquals(ColorCode.OFF, statusLED.calculateColorCode());
    }


    @Test
    public void available_charging_test() throws Exception
    {
        bookingState = BookingState.AVAILABLE;
        chargingState = ChargingState.CHARGING;

        assertEquals(ColorCode.YELLOW, statusLED.calculateColorCode());
    }

    @Test
    public void available_full_test() throws Exception
    {
        bookingState = BookingState.AVAILABLE;
        chargingState = ChargingState.FULL;

        assertEquals(ColorCode.GREEN, statusLED.calculateColorCode());
    }

    @Test
    public void available_discharging_test() throws Exception
    {
        bookingState = BookingState.AVAILABLE;
        chargingState = ChargingState.DISCHARGING;

        assertEquals(ColorCode.OFF, statusLED.calculateColorCode());
    }


    @Test
    public void blocked_charging_test() throws Exception
    {
        bookingState = BookingState.BLOCKED;
        chargingState = ChargingState.CHARGING;

        assertEquals(ColorCode.RED, statusLED.calculateColorCode());
    }

    @Test
    public void blocked_full_test() throws Exception
    {
        bookingState = BookingState.BLOCKED;
        chargingState = ChargingState.FULL;

        assertEquals(ColorCode.RED, statusLED.calculateColorCode());
    }

    @Test
    public void blocked_discharging_test() throws Exception
    {
        bookingState = BookingState.BLOCKED;
        chargingState = ChargingState.DISCHARGING;

        assertEquals(ColorCode.OFF, statusLED.calculateColorCode());
    }
}