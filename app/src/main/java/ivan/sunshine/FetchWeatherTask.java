package ivan.sunshine;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import ivan.sunshine.fragments.ForecastFragment;


public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {


    private ForecastFragment forecastFragment;

    public FetchWeatherTask(ForecastFragment forecastFragment) {
        this.forecastFragment = forecastFragment;
    }

    @Override
    protected String[] doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        String apiKey = "0f48168775da1940b5947e231b12ab89";
        int numDays = 7;

        try {
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APIKEY_PARAM = "appid";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APIKEY_PARAM, apiKey)
                    .build();
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) return null;
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            forecastJsonStr = buffer.toString();
            Log.d("logTag", forecastJsonStr);

        } catch (IOException e) {
            Log.e("ForecastFragment", "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("ForecastFragment", "Error ", e);
                }
            }
        }

        try {
            return getWeatherDataFromJson(forecastJsonStr, 7);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            forecastFragment.arrayAdapter.clear();
            forecastFragment.arrayAdapter.addAll(result);
        }
    }

    private String getReadableDateString(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM ");
        return dateFormat.format(time);
    }

    private String formatHighLows(double high, double low, String units) {

        if (units.equals(forecastFragment.getString(R.string.value_pref_units_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        }
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        return roundedHigh + "/" + roundedLow;
    }

    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        final String OWM_DT = "dt";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        String[] resultStr = new String[numDays];

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(forecastFragment.getActivity());
        String unitType = sPrefs.getString(forecastFragment.getString(R.string.key_pref_units), forecastFragment.getString(R.string.value_pref_units_metric));


        for (int i = 0; i < weatherArray.length(); i++) {
            String day;
            String description;
            String highAndLow;

            JSONObject dayForecast = weatherArray.getJSONObject(i);

            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);
            highAndLow = formatHighLows(high, low, unitType);


            day = getReadableDateString(dayForecast.getLong(OWM_DT) * 1000);
            resultStr[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStr;

    }

}
