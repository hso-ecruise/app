package ecruise.data;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tom on 21.06.2017.
 */

public class StatusRequest extends JsonRequest<JSONObject>
{

    public StatusRequest(int method, String url, JSONObject jsonRequest,
                         Response.Listener<JSONObject> listener, Response.ErrorListener errorListener)
    {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                errorListener);
    }

    public StatusRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener,
                         Response.ErrorListener errorListener)
    {
        this(jsonRequest == null ? Request.Method.GET : Request.Method.POST, url, jsonRequest,
                listener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response)
    {
        try
        {
            return Response.success(new JSONObject("{ \"code\" : \"" + Integer.toString(response.statusCode) + "\" }"), HttpHeaderParser.parseCacheHeaders(response));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }
}