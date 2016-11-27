package ivan.sunshine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ivan.sunshine.R;
import ivan.sunshine.fragments.PlaceHolderFragment;

public class DetailActivity extends AppCompatActivity {

    public static final String FORECAST_SHARE_HASHTAG = " #Sunshine App";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.activity_detail, new PlaceHolderFragment()).commit();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.detail, menu);
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//        if (actionProvider != null) {
//            actionProvider.setShareIntent(createShareForecastIntent());
//        }
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_setting) {
//            startActivity(new Intent(this, SettingActivity.class));
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(Intent.EXTRA_TEXT) + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

}
