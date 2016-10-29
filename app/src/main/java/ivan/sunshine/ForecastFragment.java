package ivan.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForecastFragment extends ListFragment {

    public ArrayAdapter arrayAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = {"Mon - Sunny - 28/24",
                "Tue - Foggy - 26/22",
                "Wed - Cloudy - 23/20",
                "Thu - Rainy - 18/15",
                "Fri - Cloudy - 20/17",
                "Sat - Sunny - 29/23",
                "Sun - Sunny - 30/26"};


        List<String> weekForecast = new ArrayList<>(Arrays.asList(forecastArray));
        arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.tv_item_forecast, weekForecast);
        setListAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            FetchWeatherTask fTask = new FetchWeatherTask();
            fTask.execute("Cherkasy");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {


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
                arrayAdapter.clear();
                arrayAdapter.addAll(result);
            }
        }

        private String getReadableDateString(long time) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM ");
            return dateFormat.format(time);
        }

        private String formatHighLows(double high, double low) {
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

            for(int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);
                highAndLow = formatHighLows(high, low);

                day = getReadableDateString(dayForecast.getLong(OWM_DT) * 1000);
                resultStr[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStr;

        }

    }
}
