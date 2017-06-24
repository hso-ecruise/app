package ecruise.common;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Aivan on 10.06.2017.
 */
public class DataParserTest {
    @Test
    public void parse() throws Exception
    {
        DataParser parser = new DataParser();
        List<List<HashMap<String, String>>> routes = null;
        routes = parser.parse(null);
        assertNull(routes);

        String data = downloadUrl();
        JSONObject jObject = new JSONObject(data);
        routes = parser.parse(jObject);
        assertNotNull(routes);
    }

    private String downloadUrl() throws IOException
    {
        LatLng orig = new LatLng(49.485000, 8.468000);
        LatLng dest = new LatLng(49.500000, 8.500000);
        String strUrl = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + orig.latitude
                + ","
                + orig.longitude
                + "&destination="
                + dest.latitude
                + ","
                + dest.longitude
                + "&sensor=false&units=metric&mode=driving&key=AIzaSyCJ32t4b_NZ1MY_dDW6XKf5hYLLZOddRVQ";
        String data = "";
        InputStream iStream;
        HttpURLConnection urlConnection = null;

        try
        {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                //Log.d("downloadUrl:LINE: ", line);
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data);
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            assert urlConnection != null;
            urlConnection.disconnect();
        }

        return data;
    }
}
