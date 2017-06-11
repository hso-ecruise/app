package me.ecruise.activitys;

import android.support.test.InstrumentationRegistry;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Jens Ullrich on 10.06.2017.
 */
public class Map2ActivityTest {


    private static final LatLng centralPos = new LatLng(49.487155, 8.466219);

    @Before
    public void setUp() throws Exception {
        assertNotNull(centralPos);
    }

    @Test
    public void onMapReady() throws Exception {

    }

    @Test
    public void setMarkerImage() throws Exception {

    }

}