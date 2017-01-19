/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */

package com.microsoft.office.sfb.healthcare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.microsoft.office.sfb.appsdk.AnonymousSession;
import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import butterknife.ButterKnife;


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
    private ConversationPropertyChangeListener mConversationPropertyChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skype_call);
        ButterKnife.inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        //Get the string parameters used to join the on premise or online meeting
        Intent messageIntent = getIntent();

        mConversation = startToJoinMeeting(
                messageIntent.getExtras().getShort(getString(R.string.onlineMeetingFlag))
                ,messageIntent.getExtras().getString(getString(R.string.discoveryUrl))
                ,messageIntent.getExtras().getString(getString(R.string.authToken))
                ,messageIntent.getExtras().getString(getString(R.string.onPremiseMeetingUrl)));

        mConversation.addOnPropertyChangedCallback(new ConversationPropertyChangeListener());

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
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
     * @param newMeetingURI
     */
    @Override
    public void onFragmentInteraction(View callView, String newMeetingURI) {
        try {
            if (newMeetingURI.contentEquals(getString(R.string.callFragmentInflated))) {
                return;
            }
            if (newMeetingURI.contentEquals(getString(R.string.leaveCall))) {
                getSupportFragmentManager().popBackStack(
                        VIDEO_FRAGMENT_STACK_STATE,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                finish();
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

            mApplication = com.microsoft.office.sfb.appsdk.Application.getInstance(this.getBaseContext());
            mApplication.getConfigurationManager().enablePreviewFeatures(true);

            mApplication.getConfigurationManager().setRequireWiFiForAudio(true);
            mApplication.getConfigurationManager().setRequireWiFiForVideo(true);
            mApplication.getConfigurationManager().setMaxVideoChannelCount(5);

            if (onlineMeetingFlag == 0){
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
        } catch (URISyntaxException ex){
            ex.printStackTrace();
            Log.e("SkypeCall", "On premise meeting uri syntax error");

        } catch (SFBException e) {
            e.printStackTrace();
            Log.e("SkypeCall", "exception on start to join meeting");

        } catch (MalformedURLException e) {
            Log.e("SkypeCall", "Online meeting url syntax error");
            e.printStackTrace();
        }
        return conversation;
    }


    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
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
