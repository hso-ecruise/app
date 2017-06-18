package me.ecruise.data;

import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Jens Ullrich on 10.06.2017.
 */
public class CarTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void setChargingLevel() throws Exception {
        final LatLng centralPos = new LatLng(49.487155, 8.466219);
        Car testCar = new Car(1, centralPos, true, true, 0, "car");
        testCar.setChargingLevel(12);
        assertEquals(0, testCar.getChargingLevel());
        testCar.setChargingLevel(34);
        assertEquals(25, testCar.getChargingLevel());
        testCar.setChargingLevel(66);
        assertEquals(50, testCar.getChargingLevel());
        testCar.setChargingLevel(88);
        assertEquals(75, testCar.getChargingLevel());
        testCar.setChargingLevel(100);
        assertEquals(100, testCar.getChargingLevel());
    }

}