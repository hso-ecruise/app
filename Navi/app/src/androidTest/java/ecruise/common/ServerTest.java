package ecruise.common;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Aivan on 10.06.2017.
 */
public class ServerTest
{
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
    public void getInstance() throws Exception
    {
        assertNull(Server.getInstance(null));
        assertNotNull(Server.getInstance(appContext));
    }


    @Test
    public void addToRequestQueue() throws Exception
    {
        String url = "https://api.ecruise.me/v1/trips";
        //final ServerRequest.VolleyCallbackObject mCallback = callback;
        final String mToken = "01aebbbdcbcf50a9b8ae1f8cf121aa2ea2bf3d2556467a7d822cea437b361780b2a2dc19a95ab7f8c91eb830e91c29e3095484588f3532f6ef7a000bb643940b";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response)
                    {
                       //Callback.onSuccess(response);
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

        Server.getInstance(appContext).addToRequestQueue(null);
        Server.getInstance(appContext).addToRequestQueue(jsObjRequest);
    }

}