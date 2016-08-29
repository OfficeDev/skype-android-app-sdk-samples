/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import android.app.FragmentTransaction;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.office.sfb.appsdk.Alert;
import com.microsoft.office.sfb.appsdk.AlertObserver;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.appsdk.Speaker;

/**
 * The Conversations Activity uses two fragments to provide Conversation & Chat functionality.
 */
public class ConversationsActivity extends AppCompatActivity implements ChatFragment.ChatFragmentInteractionListener{

    /**
     * Chat fragment for IM.
     */
    private ChatFragment chatFragment = null;

    /**
     * Participants Roster
     */
    private RosterFragment rosterFragment = null;

    /**
     * Video Fragment.
     */
    private VideoFragment videoFragment = null;

    private Conversation currentConversation = null;
    private DevicesManager devicesManager = null;

    boolean callStarted = true;
    Speaker.Endpoint endpoint = null;

    Button participantsButton = null;
    Button videoButton = null;
    LinearLayout conversationsToolbarLayout = null;
    LinearLayout alertLayout = null;
    ConversationAlertObserver conversationAlertObserver = null;

    /**
     * On creation, the activity loads the ConversationsList fragment.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        this.videoButton = (Button)findViewById(R.id.videoButtonId);
        this.participantsButton = (Button)findViewById(R.id.participantsButtonId);

        if (findViewById(R.id.fragment_container) != null) {

            // For anonymous meeting join, there will only be a single conversation in the list.
            //this.currentConversation = (com.microsoft.office.sfb.appsdk.Application.getInstance(
            //        this.getApplicationContext()).getConversationsManager().getConversations()).get(0);

            this.currentConversation = ((SFBDemoApplication)getApplication()).getAnonymousConversation();
            this.conversationAlertObserver = new ConversationAlertObserver();

            // Set callback for conversation level alerts.
            this.currentConversation.setAlertCallback(this.conversationAlertObserver);

            // Set callback for application level alerts.
            com.microsoft.office.sfb.appsdk.Application.getInstance(
                    this.getApplicationContext()).setAlertCallback(this.conversationAlertObserver);

            this.devicesManager = com.microsoft.office.sfb.appsdk.Application.getInstance(
                    this.getApplicationContext()).getDevicesManager();

            this.endpoint = this.devicesManager.getSelectedSpeaker().getActiveEndpoint();

            // Create the chat fragment.
            this.chatFragment = ChatFragment.newInstance(this.currentConversation);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, this.chatFragment, null);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            // Load the fragment.
            fragmentTransaction.commit();
        }

        this.conversationsToolbarLayout = (LinearLayout)findViewById(R.id.conversationsToolbarId);
        this.alertLayout = (LinearLayout)findViewById(R.id.alertViewId);

    }

    public void onParticipantsButtonClicked(android.view.View view) {
        this.rosterFragment = RosterFragment.newInstance(this.currentConversation);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        // Hide the current fragment.
        fragmentTransaction.hide(this.chatFragment);
        fragmentTransaction.add(R.id.fragment_container, this.rosterFragment, null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        // Add transaction to back stack so that "back" button restores state.
        fragmentTransaction.addToBackStack(null);

        // Load the fragment.
        fragmentTransaction.commit();

        this.participantsButton = (Button)view;
        participantsButton.setEnabled(false);

        this.conversationsToolbarLayout.setVisibility(View.GONE);
    }

    public void onSpeakerButtonClicked(android.view.View view) {
        switch(this.endpoint) {
            case LOUDSPEAKER:
                this.devicesManager.getSelectedSpeaker().setActiveEndpoint(Speaker.Endpoint.NONLOUDSPEAKER);
                ((Button)view).setText("Speaker On");
                break;
            case NONLOUDSPEAKER:
                this.devicesManager.getSelectedSpeaker().setActiveEndpoint(Speaker.Endpoint.LOUDSPEAKER);
                ((Button)view).setText("Speaker Off");
                break;
            default:
        }
        this.endpoint = this.devicesManager.getSelectedSpeaker().getActiveEndpoint();
    }

    public void onAudioButtonClicked(android.view.View view) {
        if (!this.callStarted) {
            if (this.currentConversation.getAudioService().canStart()) {
                try {
                    this.currentConversation.getAudioService().start();
                    ((Button)view).setText("End Call");
                    this.callStarted = true;
                } catch (SFBException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (this.currentConversation.getAudioService().canStop()) {
                try {
                    this.currentConversation.getAudioService().stop();
                    ((Button)view).setText("Call");
                    this.callStarted = false;
                } catch (SFBException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void onDismissButtonClicked(android.view.View view) {
        LinearLayout alertLayout = (LinearLayout)view.getParent();
        alertLayout.setVisibility(View.GONE);
    }

    public void onVideoButtonClicked(android.view.View view) {
        this.videoFragment = VideoFragment.newInstance(this.currentConversation, devicesManager);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        // Hide the current fragment.
        fragmentTransaction.hide(this.chatFragment);
        fragmentTransaction.add(R.id.fragment_container, this.videoFragment, null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        // Add transaction to back stack so that "back" button restores state.
        fragmentTransaction.addToBackStack(null);

        // Load the fragment.
        fragmentTransaction.commit();

        this.videoButton = (Button)view;
        videoButton.setEnabled(false);

        this.conversationsToolbarLayout.setVisibility(View.GONE);
    }

    /**
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * onPause
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * onStop
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Process "back" button press.
     */
    @Override
    public void onBackPressed() {
        // If the chat fragment is loaded, pressing the back button pops the conversationsList fragment.
        getFragmentManager().popBackStack();

        int count = getFragmentManager().getBackStackEntryCount();

        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof RosterFragment || currentFragment instanceof VideoFragment) {
            this.participantsButton.setEnabled(true);
            this.videoButton.setEnabled(true);
            this.conversationsToolbarLayout.setVisibility(View.VISIBLE);
        }

        // If you are on the first loaded fragment let the super class handle the back button event.
        if (count == 0) {
            super.onBackPressed();
        }
    }

    /**
     * The ChatFragment calls this callback method for changes to report to the activity.
     */
    @Override
    public void onChatFragmentInteraction() {
        // Dummy method provided for demonstration
    }

    public void showAlert(Alert alert) {
        this.alertLayout.setVisibility(View.VISIBLE);
        TextView alertTypeText = (TextView)this.alertLayout.findViewById(R.id.alertTextViewId);
        alertTypeText.setText(alert.getAlertType().toString());
    }

    private class ConversationAlertObserver extends AlertObserver.OnAlertCallback {
        @Override
        public void onAlert(Alert alert) {
            showAlert(alert);
        }
    }
}
