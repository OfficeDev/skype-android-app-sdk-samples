/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */

package com.microsoft.office.sfb.healthcare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.microsoft.office.sfb.healthcare.R.id.toolbar;


public class wellbabyreport extends AppCompatActivity   {
    Intent mSkypeCallIntent = null;
    @InjectView(toolbar)
    protected Toolbar mToolBar;
    android.support.v4.app.DialogFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wellbabyreport);

        ButterKnife.inject(this);

        setSupportActionBar(mToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    @OnClick(R.id.fab)
    public void onClick() {
        try {
            Intent callIntent = new Intent(this, SkypeCall.class);
            startActivity(callIntent);
        } catch(RuntimeException e){
            e.printStackTrace();
        }


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
