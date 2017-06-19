package ecruise.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Tom
 * @since 27.04.2017.
 */
public class ScanLED_Test
{
    private ScanLED scanLED = new ScanLED();

    @Test
    public void charging_available() throws Exception
    {
        assertEquals(ColorCode.YELLOW, scanLED.getColorCode(CarState.AVAILABLE_CHARGING, false));
        assertEquals(ColorCode.YELLOW, scanLED.getColorCode(CarState.AVAILABLE_CHARGING, true));
    }

    @Test
    public void booked_me() throws Exception
    {
        assertEquals(ColorCode.GREEN, scanLED.getColorCode(CarState.BOOKED, true));
    }

    @Test
    public void booked_other() throws Exception
    {
        assertEquals(ColorCode.RED, scanLED.getColorCode(CarState.BOOKED, false));
    }

    @Test
    public void full_available() throws Exception
    {
        assertEquals(ColorCode.GREEN, scanLED.getColorCode(CarState.AVAILABLE_FULL, false));
        assertEquals(ColorCode.GREEN, scanLED.getColorCode(CarState.AVAILABLE_FULL, true));
    }

    @Test
    public void blocked() throws Exception
    {
        assertEquals(ColorCode.RED, scanLED.getColorCode(CarState.BLOCKED, false));
        assertEquals(ColorCode.RED, scanLED.getColorCode(CarState.BLOCKED, true));
    }
}