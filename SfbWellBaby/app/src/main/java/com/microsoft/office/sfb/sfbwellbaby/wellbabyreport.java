package com.microsoft.office.sfb.sfbwellbaby;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.microsoft.office.sfb.sfbwellbaby.R.id.toolbar;


public class wellbabyreport extends AppCompatActivity {
    Intent videoIntent = null;
    @InjectView(toolbar)
    protected Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wellbabyreport);

        ButterKnife.inject(this);

        setSupportActionBar(mToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @OnClick(R.id.fab)
    public void onClick() {
        videoIntent = new Intent(this, SkypeCall.class);
        startActivity(videoIntent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wellbabyreport, menu);


        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                    android.app.Fragment prev = getFragmentManager().findFragmentByTag("settings");
                    if (prev != null)
                        ft.remove(prev);

                    ft.addToBackStack(null);

                    DialogFragment settingsFragment = SettingsDialog.newInstance("https://meet.lync.com/microsoft/johnau/H7TW81MG");
                    settingsFragment.show(getSupportFragmentManager(), "settings");
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }

        } catch (Throwable t) {
            if (t.getMessage() == null)
                Log.e("Asset", " ");
            else
                Log.e("Asset", t.getMessage());
        }
        return true;

    }
}
