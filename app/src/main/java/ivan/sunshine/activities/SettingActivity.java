package ivan.sunshine.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ivan.sunshine.R;
import ivan.sunshine.fragments.SettingFragment;

public class SettingActivity extends AppCompatActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.activity_setting, new SettingFragment()).commit();
        }
    }


}
