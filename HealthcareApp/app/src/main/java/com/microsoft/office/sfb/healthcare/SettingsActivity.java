package com.microsoft.office.sfb.healthcare;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
        PreferenceManager.setDefaultValues(SettingsActivity.this, R.xml.preferences, false);
    }

    @Override
    public void onFragmentInteraction() {

    }
}
