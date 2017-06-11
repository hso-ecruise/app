package me.ecruise.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;


import static org.junit.Assert.*;

/**
 * Created by Aivan on 10.06.2017.
 */
@RunWith(AndroidJUnit4.class)
public class ServerTest
{
    Context appContext;
    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        assertNotNull(appContext);
        assertNull(Server.getInstance(null));
        assertNotNull(Server.getInstance(appContext));
        assertNotNull(Server.getInstance(null));
        assertNotNull(Server.getInstance(appContext));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getInstance() throws Exception
    {

    }

    @Test
    public void addToRequestQueue() throws Exception
    {
        String url = "https://api.ecruise.me/v1/trips";
        final String mToken = "01aebbbdcbcf50a9b8ae1f8cf121aa2ea2bf3d2556467a7d822cea437b361780b2a2dc19a95ab7f8c91eb830e91c29e3095484588f3532f6ef7a000bb643940b";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response)
                    {
                        assertNotNull(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", mToken);
                return params;
            }
        };
        Server.getInstance(appContext).addToRequestQueue(jsObjRequest);
    }

}