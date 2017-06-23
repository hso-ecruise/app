package ecruise.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class StatusLED_Test
{

    private StatusLED statusLED = new StatusLED();

    @Test
    public void booked_test() throws Exception
    {
        assertEquals(ColorCode.BLUE, statusLED.getColorCode(CarState.BOOKED));
    }

    @Test
    public void available_charging_test() throws Exception
    {
        assertEquals(ColorCode.YELLOW, statusLED.getColorCode(CarState.AVAILABLE_CHARGING));
    }

    @Test
    public void available_full_test() throws Exception
    {
        assertEquals(ColorCode.GREEN, statusLED.getColorCode(CarState.AVAILABLE_FULL));
    }

    @Test
    public void available_discharging_test() throws Exception
    {
        assertEquals(ColorCode.OFF, statusLED.getColorCode(CarState.DRIVING));
    }


    @Test
    public void blocked_test() throws Exception
    {
        assertEquals(ColorCode.RED, statusLED.getColorCode(CarState.BLOCKED));
    }
}