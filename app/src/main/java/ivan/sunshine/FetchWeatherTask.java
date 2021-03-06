package ivan.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

import ivan.sunshine.data.WeatherContract;
import ivan.sunshine.fragments.ForecastFragment;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    //private final String TAG = "tag";

    private ForecastFragment forecastFragment;
    private Context context;

    public FetchWeatherTask(ForecastFragment forecastFragment, Context context) {
        this.forecastFragment = forecastFragment;
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }


        String forecastJsonStr;

        String format = "json";
        String units = "metric";
        String apiKey = "0f48168775da1940b5947e231b12ab89";
        int numDays = 7;

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
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(builtUri.toString()).build();
        Response response;
        try {
            response = client.newCall(request).execute();
            forecastJsonStr = response.body().string();
            getWeatherDataFromJson(forecastJsonStr, params[0]);
//            return getWeatherDataFromJson(forecastJsonStr, 7, params[0]);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
//    protected void onPostExecute(String[] result) {
//        if (result != null) {
//            forecastFragment.arrayAdapter.clear();
//            forecastFragment.arrayAdapter.addAll(result);
//        }
//    }





    private void getWeatherDataFromJson(String forecastJsonStr, String locationSetting)
            throws JSONException {

        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        final String OWM_DT = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        final String OWM_WEATHER_ID = "id";
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);
        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);
        long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

        Time dayTime = new Time();
        dayTime.setToNow();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        dayTime = new Time();

        for(int i = 0; i < weatherArray.length(); i++) {
            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            JSONObject dayForecast = weatherArray.getJSONObject(i);

            dateTime = dayTime.setJulianDay(julianStartDay+i);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);
        }

        int inserted = 0;

        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = context.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
        }

    }

    private long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;
        Cursor locationCursor = forecastFragment.getContext().getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null
        );
        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri insertedUri = forecastFragment.getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues);
            locationId = ContentUris.parseId(insertedUri);
        }
        locationCursor.close();
        return locationId;
    }



}
