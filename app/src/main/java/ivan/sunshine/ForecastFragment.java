package ivan.sunshine;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.Arrays;
import java.util.List;

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
            FetchWeatherTask fTask = new FetchWeatherTask(this);
            fTask.execute("Cherkasy");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, (String) arrayAdapter.getItem(position)));
    }
}
