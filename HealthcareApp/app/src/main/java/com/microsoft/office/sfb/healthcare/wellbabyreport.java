/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */

package com.microsoft.office.sfb.healthcare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.office.sfb.appsdk.AnonymousSession;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.microsoft.office.sfb.healthcare.R.id.fab;
import static com.microsoft.office.sfb.healthcare.R.id.toolbar;

/*
The main app UI.

This class supports the Skype
REST calls to a service application to get a meeting url, discover URL, and
 authentication token are made in this class. The re
 */
public class wellbabyreport extends AppCompatActivity  {
    Intent mSkypeCallIntent = null;
    @InjectView(toolbar)
    protected Toolbar mToolBar;

	@InjectView(fab)
	protected FloatingActionButton mfloatyButton;
    android.support.v4.app.DialogFragment mSettingsFragment;
    protected TextView mResponseBody;
    LinearLayout mAlertLayout = null;
    View mRootView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wellbabyreport);

        mRootView = this.findViewById(android.R.id.content).getRootView();

        ButterKnife.inject(this);

        setSupportActionBar(mToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

	@Override
	protected void onPostResume() {
		super.onPostResume();
		mfloatyButton.show();
		onResumeFragments();
	}

    @OnClick(fab)
    public void onClick() {
        try {

			Snackbar.make(mRootView, "Calling your doctor... stand by", Snackbar.LENGTH_LONG)
					.setAction("Action", null).show();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Boolean promptedFlag = sharedPreferences.getBoolean(getString(R.string.promptedForLicense),false);
            Boolean acceptedFlag = sharedPreferences.getBoolean(getString(R.string.acceptedVideoLicense),false);

            //If user has never been prompted for the video license or User has been prompted and has
            // accepted the license, start the call. User is prompted for
            //license in the call activity
            if (!promptedFlag || acceptedFlag == true){
                //run the call
                startCallActivity(sharedPreferences);
            //User has been prompted and declined the license
            } else {
                AlertDialog.Builder alertDialogBuidler = new AlertDialog.Builder(this);
                alertDialogBuidler.setTitle("Video License");
                alertDialogBuidler.setMessage("Video codec license was rejected. The call cannot start.");
                alertDialogBuidler.setNeutralButton("Ok",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialogBuidler.show();

            }

        } catch(RuntimeException e){
            e.printStackTrace();
        }


    }

    private void startCallActivity(SharedPreferences sharedPreferences){
        if (sharedPreferences.getBoolean(getString(R.string.SfBOnlineFlag), false) == false) {
            Intent callIntent = new Intent(this, SkypeCall.class);
            Bundle meetingParameters = new Bundle();
            meetingParameters.putShort(getString(R.string.onlineMeetingFlag), (short) 0);
            meetingParameters.putString(getString(R.string.discoveryUrl), "");
            meetingParameters.putString(getString(R.string.authToken), "");
            meetingParameters.putString(getString(R.string.onPremiseMeetingUrl)
                    , getString(R.string.meeting_url));
            callIntent.putExtras(meetingParameters);

            startActivity(callIntent);

        } else {
            mfloatyButton.hide();
            //Start SfB Online flow
            startToJoinOnlineMeeting();
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
                    Intent settingsIntent = new Intent(getBaseContext(),SettingsActivity.class);
                    startActivity(settingsIntent);

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
    /**
     * Get the UCWA discovery URL and an authentication token from a UCAP powered SaaS application
     */
    @SuppressLint("LongLogTag")
    private  void startToJoinOnlineMeeting() {

		mfloatyButton.hide();
        // startMethodTracing("startMeeting");



        final URI meetingURI = null;
        AnonymousSession conversation = null;
        try {

            //Retrofit 2 object for making REST calls over https
            RESTUtility rESTUtility = new RESTUtility(this,getString(R.string.cloudAppBaseurl));

            //Get the Middle Tier helpdesk app interface for making REST call
            final RESTUtility.SaasAPIInterface apiInterface = rESTUtility.getSaaSClient();

            String body = "Subject=adhocMeeting&Description=adhocMeeting&AccessLevel=";

            RequestBody bridgeRequest = RequestBody.create(
                    MediaType.parse("text/plain, */*; q=0.01"),
                    body);


            Call<GetMeetingURIResult> call = apiInterface.getAdhocMeeting(bridgeRequest);
            call.enqueue(new Callback<GetMeetingURIResult>() {
                @Override
                public void onResponse(Call<GetMeetingURIResult> call, Response<GetMeetingURIResult> response) {
                    if (null != response.body()) {
                        try {

                            if (response.body().JoinUrl != null){
                                GetAnonymousToken(apiInterface, response.body().JoinUrl);
                            } else {
                                Snackbar.make(mRootView, "Meeting URI was not returned", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();

                            }


                        } catch (Exception e) {
                            if (null != response.body()) {
                                // body wasn't JSON
                                mResponseBody.setText(response.body().toString());
                            } else {
                                // set the stack trace as the response body
                                displayThrowable(e);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<GetMeetingURIResult> call, Throwable t) {
                    Log.i("Failed to get meeting url", t.getLocalizedMessage().toString());
					Snackbar.make(mRootView, "Failed: Could not get a meeting url", Snackbar.LENGTH_LONG)
							.setAction("Action", null).show();
					mfloatyButton.show();


				}
            });
        } catch (UnsupportedOperationException e) {
            Log.e("unsupported operation",
                    e.getLocalizedMessage());
        } catch (Exception ex) {
            Log.e("Exception on get SaaS interface",
                    ex.getLocalizedMessage());

        }

        return;
    }

    @SuppressLint("LongLogTag")
    private void GetAnonymousToken(RESTUtility.SaasAPIInterface apiInterface, String meetingUri) {
        try {
            String body = String.format(
                    "ApplicationSessionId=%s&AllowedOrigins=%s&MeetingUrl=%s"
                    ,UUID.randomUUID()
                    ,"http%3A%2F%2Fsdksamplesucap.azurewebsites.net%2F"
                    ,meetingUri);
            RequestBody bridgeRequest = RequestBody.create(
                    MediaType.parse("text/plain, */*; q=0.01"),
                    body);

            Call<GetTokenResult> callforBridge = apiInterface.getAnonymousToken(
                    bridgeRequest);
            callforBridge.enqueue(new Callback<GetTokenResult>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onResponse(Call<GetTokenResult> call, final Response<GetTokenResult> response) {
                    Log.i("Succeeded in starting chat bridge", "");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent callIntent = new Intent(getApplicationContext(), SkypeCall.class);
                            Bundle meetingParameters = new Bundle();
                            meetingParameters.putShort(getString(R.string.onlineMeetingFlag), (short) 1);
                            meetingParameters.putString(getString(R.string.discoveryUrl), response.body().DiscoverUri);
                            meetingParameters.putString(getString(R.string.authToken), response.body().Token);
                            meetingParameters.putString(getString(R.string.onPremiseMeetingUrl),"");
                            callIntent.putExtras(meetingParameters);
                            startActivity(callIntent);

                        }
                    });

                }

                @Override
                public void onFailure(Call<GetTokenResult> call, Throwable t) {
                    Log.i("failed token get", t.getLocalizedMessage().toString());
					Snackbar.make(mRootView, "Authentication token was not returned", Snackbar.LENGTH_LONG)
							.setAction("Action", null).show();

                }
            });

        } catch (UnsupportedOperationException e) {
            Log.e("unsupported operation",
                    e.getLocalizedMessage());
        } catch (Exception ex) {
            Log.e("Exception on get SaaS interface",
                    ex.getLocalizedMessage());

        }

    }

    private void displayThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String trace = sw.toString();
        mResponseBody.setText(trace);
    }


}
