/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */

package com.microsoft.office.sfb.healthcare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.microsoft.office.sfb.appsdk.AnonymousSession;
import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * SkypeCall activity hosts one fragment. The activity shows a progress indicator and is loaded
 * immediately after this activity is inflated.  When the ConversationPropertyChangeListener reports that
 * the conversation is established, SkypeCall loads the SkypeCallFragment. The SkypeCallFragment hosts the
 * incoming video stream and a preview
 * of the outgoing stream.
 * SkypeCall finds the containing views in the SkypeCallFragment and provides those container views to
 * SkypeManagerImpl. SkypeManagerImpl sends the two video streams to the UI via those containers.
 */
public class SkypeCall extends AppCompatActivity
        implements
        SkypeCallFragment.OnFragmentInteractionListener {

    SkypeCallFragment mCallFragment = null;
    Application mApplication;
    private static final String VIDEO_FRAGMENT_STACK_STATE = "videoFragment";
    Conversation mConversation;
    AnonymousSession mAnonymousSession = null;
    public MenuItem mCameraToggleItem;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skype_call);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        //Get the string parameters used to join the on premise or online meeting
        Intent messageIntent = getIntent();

        mConversation = startToJoinMeeting(
                messageIntent.getExtras().getShort(getString(R.string.onlineMeetingFlag))
                , messageIntent.getExtras().getString(getString(R.string.discoveryUrl))
                , messageIntent.getExtras().getString(getString(R.string.authToken))
                , messageIntent.getExtras().getString(getString(R.string.onPremiseMeetingUrl)));

        mConversation.addOnPropertyChangedCallback(new ConversationPropertyChangeListener());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_skype_call, menu);
        mCameraToggleItem = menu.findItem(R.id.changeCamera);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {

                case android.R.id.home:
                    if (mConversation.canLeave())
                        try {
                            mConversation.leave();
                        } catch (SFBException e) {
                            e.printStackTrace();
                        }

                    NavUtils.navigateUpFromSameTask(this);
                case R.id.pauseVideoMenuItem:

                case R.id.muteAudioMenuItem:

                case R.id.changeCamera:
                    if (mConversation.getVideoService().canSetActiveCamera() == true) {
                        mCallFragment.mConversationHelper.changeActiveCamera();
                    }
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


    private void loadCallFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        try {
            mCallFragment = SkypeCallFragment.newInstance(
                    mConversation,
                    mApplication
                            .getDevicesManager());
            fragmentTransaction.add(
                    R.id.fragment_container,
                    mCallFragment,
                    "video");
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(VIDEO_FRAGMENT_STACK_STATE);
            fragmentTransaction.commitAllowingStateLoss();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoked from SkypeCallFragment when inflated. Provides the TextureView for preview to the
     * SkypeManagerImpl
     *
     * @param callView
     * @param fragmentAction
     */
    @Override
    public void onFragmentInteraction(View callView, String fragmentAction) {
        try {
            if (fragmentAction.contentEquals(getString(R.string.callFragmentInflated))) {
                return;
            }
            if (fragmentAction.contentEquals(getString(R.string.leaveCall))) {
                getSupportFragmentManager().popBackStack(
                        VIDEO_FRAGMENT_STACK_STATE,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                finish();
            }
            if (fragmentAction.contentEquals(getString(R.string.canToggleCamera))) {
                //enable menu item
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //Toggle the enable state of the Change Camera
                        //menu option
                        if (mCameraToggleItem != null)
                            mCameraToggleItem.setEnabled(!mCameraToggleItem.isEnabled());
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Connect to an existing Skype for Business meeting with the URI you get
     * from a server-side UCWA-based web service.
     */
    private Conversation startToJoinMeeting(
            Short onlineMeetingFlag
            , String discoveryUrl
            , String authToken
            , String meetingUrl) {
        Conversation conversation = null;
        try {

            mApplication = Application.getInstance(this.getBaseContext());
            mApplication.getConfigurationManager().enablePreviewFeatures(
                    PreferenceManager
                            .getDefaultSharedPreferences(this)
                            .getBoolean(getString(R.string.enablePreviewFeatures),false));
            mApplication.getConfigurationManager().setRequireWiFiForAudio(true);
            mApplication.getConfigurationManager().setRequireWiFiForVideo(true);
            mApplication.getConfigurationManager().setMaxVideoChannelCount(
                    Long.parseLong(PreferenceManager
                            .getDefaultSharedPreferences(this)
                            .getString(getString(R.string.maxVideoChannels),"5")));

            if (onlineMeetingFlag == 0) {
                mAnonymousSession = mApplication
                        .joinMeetingAnonymously(
                                getString(R.string.userDisplayName)
                                , new URI(meetingUrl));

            } else {
                mAnonymousSession = mApplication
                        .joinMeetingAnonymously(
                                getString(R.string.userDisplayName)
                                , new URL(discoveryUrl)
                                , authToken);
            }
            conversation = mAnonymousSession.getConversation();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            Log.e("SkypeCall", "On premise meeting uri syntax error");
        } catch (SFBException e) {
            e.printStackTrace();
            Log.e("SkypeCall", "exception on start to join meeting");
        } catch (MalformedURLException e) {
            Log.e("SkypeCall", "Online meeting url syntax error");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("SkypeCall", "Exception");
            e.printStackTrace();
        }
        return conversation;
    }


    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        finish();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SkypeCall Page")
                // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    /**
     * Callback implementation for listening for conversation property changes.
     */
    class ConversationPropertyChangeListener extends
            Observable.OnPropertyChangedCallback {
        ConversationPropertyChangeListener() {
        }

        /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            Conversation conversation = (Conversation) sender;
            if (propertyId == Conversation.STATE_PROPERTY_ID) {
                if (conversation.getState() == Conversation.State.ESTABLISHED) {
                    Log.e("SkypeCall", conversation
                            .getMeetingInfo()
                            .getMeetingDescription()
                            + " is established");
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadCallFragment();
                                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("SkypeCall"
                                , "exception on meeting started");
                    }
                }
            }
        }
    }
}
