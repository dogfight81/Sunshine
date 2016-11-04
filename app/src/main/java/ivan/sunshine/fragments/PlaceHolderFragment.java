package ivan.sunshine.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ivan.sunshine.R;


public class PlaceHolderFragment extends Fragment {

    TextView tvDetailForecast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_place_holder, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDetailForecast = (TextView) view.findViewById(R.id.tv_detail_forecast);
        tvDetailForecast.setText(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));

    }
}
