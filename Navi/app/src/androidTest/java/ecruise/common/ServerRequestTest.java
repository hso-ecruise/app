package ecruise.common;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Aivan on 10.06.2017.
 */
public class ServerRequestTest {
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
    public void generateJsonArray() throws Exception {

        ServerRequest sr = new ServerRequest(appContext);
        sr.generateJsonArray("https://api.ecruise.me/v1/charging-stations",
                new ServerRequest.VolleyCallbackArray()
                {
                    @Override
                    public void onSuccess(JSONArray result)
                    {
                        try
                        {
                            assertNotNull(result);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
    }

}