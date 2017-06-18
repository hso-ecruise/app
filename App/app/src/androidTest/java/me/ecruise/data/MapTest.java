package me.ecruise.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Jens Ullrich on 10.06.2017.
 */
public class MapTest {
    Context appContext;
    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        assertNotNull(appContext);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getInstance() throws Exception {
        Map testMap;
        Map testMap2;
        testMap = Map.getInstance(appContext);
        assertNotNull(testMap);
        testMap2 = Map.getInstance(appContext);
        assertNotNull(testMap2);
        assertEquals(testMap, testMap2);
    }

    @Test
    public void saveStations() throws Exception {
        JSONArray array = new JSONArray();
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        obj1.put("latitude", 1.11);
        obj1.put("longitude", 2.22);
        obj1.put("chargingStationId", 1);
        obj1.put("slots", 3);
        obj1.put("slotsOccupied", 3);
        obj2.put("latitude", 3.33);
        obj2.put("longitude", 4.44);
        obj2.put("chargingStationId", 2);
        obj2.put("slots", 3);
        obj2.put("slotsOccupied", 1);
        array.put(obj1);
        array.put(obj2);
        Map.getInstance(appContext).saveStations(array, new Customer.DataCallback() {
            @Override
            public void onSuccess() {
                assertTrue(Map.getInstance(appContext).getStations().size() == 2);
            }

            @Override
            public void onFailure() {

            }
            });
    }

    @Test
    public void saveCars() throws Exception {
        JSONArray array = new JSONArray();
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        JSONObject obj3 = new JSONObject();

        obj1.put("lastKnownPositionLatitude", 1.11);
        obj1.put("lastKnownPositionLongitude", 2.22);
        obj1.put("carId", 1);
        obj1.put("chargingState", 3);
        obj1.put("bookingState", 3);
        obj1.put("chargeLevel", 44);

        obj2.put("lastKnownPositionLatitude", 3.33);
        obj2.put("lastKnownPositionLongitude", 4.44);
        obj2.put("carId", 2);
        obj2.put("chargingState", 1);
        obj2.put("bookingState", 1);
        obj2.put("chargeLevel", 1);

        obj3.put("lastKnownPositionLatitude", 3.33);
        obj3.put("lastKnownPositionLongitude", 4.44);
        obj3.put("carId", 2);
        obj3.put("chargingState", 3);
        obj3.put("bookingState", 1);
        obj3.put("chargeLevel", 1);

        array.put(obj1);
        array.put(obj2);
        array.put(obj3);
        Map.getInstance(appContext).saveCars(array, new Customer.DataCallback() {
            @Override
            public void onSuccess() {
                assertTrue(Map.getInstance(appContext).getCars().size() == 2);
            }

            @Override
            public void onFailure() {

            }
        });
    }
}