package ecruise.logic;

import ecruise.data.BookingState;
import ecruise.data.ChargingState;

/**
 * Created by Tom on 21.03.2017.
 */
public enum CarState
{
    BOOKED_FULL, BOOKED_CHARGING, AVAILABLE_CHARGING, AVAILABLE_FULL, BLOCKED, DISCHARGING
}