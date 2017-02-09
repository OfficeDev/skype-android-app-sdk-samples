/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */

package com.microsoft.office.sfb.healthcare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.appindexing.Thing;
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

    private SkypeCallFragment mCallFragment = null;
    private com.microsoft.office.sfb.appsdk.Application mApplication;
    private static final String VIDEO_FRAGMENT_STACK_STATE = "videoFragment";
    private Conversation mConversation;
    private AnonymousSession mAnonymousSession = null;
    private MenuItem mCameraToggleItem;
    private MenuItem mVideoPauseToggleItem;
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

        //Meetings can be hosted in SfB Online or Sfb on premise. These
        //intent extras communicate the necessary parameters for the
        //Skype call to be placed to the correct endpoint.
        mConversation = startToJoinMeeting(
                messageIntent.getExtras().getShort(getString(R.string.onlineMeetingFlag))
                , messageIntent.getExtras().getString(getString(R.string.discoveryUrl))
                , messageIntent.getExtras().getString(getString(R.string.authToken))
                , messageIntent.getExtras().getString(getString(R.string.onPremiseMeetingUrl)));

        mConversation.addOnPropertyChangedCallback(new ConversationPropertyChangeListener());


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
        mVideoPauseToggleItem = menu.findItem(R.id.pauseVideoMenuItem);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {

                case android.R.id.home:
                    if (mConversation.canLeave())
                        try {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .detach(mCallFragment)
                                    .commit();
                            mConversation.leave();
                        } catch (SFBException e) {
                            e.printStackTrace();
                        }

                    NavUtils.navigateUpFromSameTask(this);
                    break;
                case R.id.pauseVideoMenuItem:
                    if (mConversation.getVideoService().canSetPaused() == true) {
                        Log.i(
                                "SkypeCall",
                                "select pauseVideoMenuItem "
                                        );

                        mCallFragment.mConversationHelper.toggleVideoPaused();
                    }

                    break;
                case R.id.muteAudioMenuItem:

                    break;
                case R.id.changeCamera:
                    if (mConversation.getVideoService().canSetActiveCamera() == true) {
                        mCallFragment.mConversationHelper.changeActiveCamera();
                    }
                    break;
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
            if (fragmentAction.contentEquals(getString(R.string.callEstablished))) {

            }
            if (fragmentAction.contentEquals(getString(R.string.canToggleCamera))) {
                //Toggle the enable state of the Change Camera
                //menu option
                if (mCameraToggleItem != null)
                    mCameraToggleItem.setEnabled(!mCameraToggleItem.isEnabled());
            }
            if (fragmentAction.contentEquals(getString(R.string.pauseVideo))) {
                if (mVideoPauseToggleItem != null)
                    mVideoPauseToggleItem.setTitle(R.string.pauseVideo);
            }
            if (fragmentAction.contentEquals(getString(R.string.resume_video))) {
                if (mVideoPauseToggleItem != null)
                    mVideoPauseToggleItem.setTitle(R.string.resume_video);
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
                            .getBoolean(getString(R.string.enablePreviewFeatures), false));
            mApplication.getConfigurationManager().setRequireWiFiForAudio(true);
            mApplication.getConfigurationManager().setRequireWiFiForVideo(true);
            mApplication.getConfigurationManager().setMaxVideoChannelCount(
                    Long.parseLong(PreferenceManager
                            .getDefaultSharedPreferences(this)
                            .getString(getString(R.string.maxVideoChannels), "5")));

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            if (!sharedPreferences.getBoolean(getString(R.string.acceptedVideoLicense),false)) {
                AlertDialog.Builder alertDialogBuidler = new AlertDialog.Builder(this);
                alertDialogBuidler.setTitle("Video License");
                alertDialogBuidler.setMessage(getString(R.string.videoCodecTerms));
                alertDialogBuidler.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mApplication.getConfigurationManager().setEndUserAcceptedVideoLicense();
                        setLicenseAcceptance(true);
                    }
                });
                alertDialogBuidler.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setLicenseAcceptance(false);

                    }
                });
                alertDialogBuidler.show();

            }

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


    /**
     * Writes the user's acceptance or rejection of the video license
     * presented in the alert dialog
     * @param userChoice  Boolean, the user's license acceptance choice
     */
    private void setLicenseAcceptance(Boolean userChoice){
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putBoolean(
                        getString(
                                R.string.acceptedVideoLicense)
                        ,userChoice).apply();

    }
    @Override
    protected void onStop() {
        super.onStop();
        finish();
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
                        loadCallFragment();

                        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Log.e("SkypeCall"
                                , "exception on meeting started");
                    }
                }
            }
        }
    }
}
