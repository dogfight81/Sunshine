package ivan.sunshine.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import ivan.sunshine.FetchWeatherTask;
import ivan.sunshine.R;
import ivan.sunshine.activities.DetailActivity;

public class ForecastFragment extends ListFragment implements AdapterView.OnItemClickListener {

    public ArrayAdapter arrayAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.tv_item_forecast, new ArrayList<String>());
        setListAdapter(arrayAdapter);
        getListView().setOnItemClickListener(this);
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, (String) arrayAdapter.getItem(position)));
    }

    private void updateWeather() {
        FetchWeatherTask fTask = new FetchWeatherTask(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = preferences.getString(getString(R.string.key_pref_location), getString(R.string.value_pref_location_default));
        fTask.execute(location);
    }
}
