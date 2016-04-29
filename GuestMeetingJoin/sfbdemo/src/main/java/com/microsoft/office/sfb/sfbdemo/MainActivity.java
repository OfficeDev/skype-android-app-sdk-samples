/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import java.net.URI;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.ConfigurationManager;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.appsdk.DevicesManager;

/**
 * Main Activity of the app.
 * The activity provides UI to join the meeting and navigate to the conversations view.
 */
public class MainActivity extends AppCompatActivity {

    Application application = null;
    ConfigurationManager configurationManager = null;
    DevicesManager devicesManager = null;
    ConversationPropertyChangeListener conversationPropertyChangeListener = null;
    Conversation anonymousConversation = null;

    TextView conversationStateTextView = null;
    Button joinMeetingButton = null;

    private Intent conversationsIntent = null;

    boolean meetingJoined = false;

    /**
     * Creating the activity initializes the SDK Application instance.
     * @param savedInstanceState saved instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.application = Application.getInstance(this.getApplication().getApplicationContext());
        this.devicesManager = application.getDevicesManager();
        this.configurationManager = application.getConfigurationManager();

        // Note that the sample enable video over cellular network. This is not the default.
        this.configurationManager.setRequireWiFiForVideo(false);

        // Get UI elements.
        this.conversationStateTextView = (TextView)findViewById(R.id.statusTextViewId);
        this.joinMeetingButton = (Button)findViewById(R.id.joinMeetingButtonId);
        this.conversationsIntent = new Intent(this,ConversationsActivity.class);

        this.updateUiState();
    }

    @Override
    protected void onDestroy() {
        this.configurationManager = null;
        this.application = null;

        super.onDestroy();
    }

    /**
     * Navigate to the conversations list view.
     * Note that, the conversations list view is provided only for demonstration purposes.
     * For anonymous meeting join it will always have a single conversation after meeting join is
     * successful.
     * @param view View
     */
    public void onConversationsButtonClick(android.view.View view) {
        this.navigateToConversationsActivity();
    }

    /**
     * The click handler joins or leaves the meeting based on current state.
     *
     * @param view View.
     */
    public void onJoinMeetingButtonClick(android.view.View view) {
        // Hide keyboard
        InputMethodHelper.hideSoftKeyBoard(this.getApplication().getApplicationContext(),
                view.getWindowToken());

        if (meetingJoined) {
            // Leave the meeting.
            try {
                this.anonymousConversation.leave();
                this.meetingJoined = false;
                this.updateUiState();

            } catch (SFBException e) {
                e.printStackTrace();
            }
        } else {
            //Join the meeting.
            // Get the display name.
            final android.widget.TextView displayNameTextView =
                    (android.widget.TextView) findViewById(R.id.displayNameEditTextId);

            // Get the meeting uri
            final android.widget.EditText joinMeetingEditText =
                    (android.widget.EditText) findViewById(R.id.meetingUriEditTextId);
            String meetingUriString = joinMeetingEditText.getText().toString();
            URI meetingUri = URI.create(meetingUriString);

            // Join meeting and monitor conversation state to determine meeting join completion.
            try {

                // Set the default device to Speaker
                //this.devicesManager.setActiveEndpoint(DevicesManager.Endpoint.LOUDSPEAKER);

                this.anonymousConversation = this.application.joinMeetingAnonymously(
                        displayNameTextView.getText().toString(), meetingUri);

                SFBDemoApplication application = (SFBDemoApplication)getApplication();
                application.setAnonymousConversation(this.anonymousConversation);

                // Conversation begins in Idle state. It will move from Idle->Establishing->InLobby/Established
                // depending on meeting configuration.
                // We will monitor property change notifications for State property.
                // Once the conversation is Established, we will move to the next activity.
                this.conversationPropertyChangeListener = new ConversationPropertyChangeListener();
                this.anonymousConversation.addOnPropertyChangedCallback(this.conversationPropertyChangeListener);
            } catch (SFBException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update the UI state.
     */
    public void updateUiState() {
        if (meetingJoined) {
            this.joinMeetingButton.setText(R.string.leave_meeting);
        } else {
            this.joinMeetingButton.setText(R.string.join_meeting);
            //conversationStateTextView.setText("");
        }
    }

    /**
     * Navigate to the Conversations activity.
     */
    private void navigateToConversationsActivity() {
        startActivity(this.conversationsIntent);
    }

    /**
     * Determines meeting join state based on conversations state.
     */
    public void updateConversationState() {
        Conversation.State state = this.anonymousConversation.getState();
        conversationStateTextView.setText(state.toString());
        switch (state) {
            case ESTABLISHED:
                this.meetingJoined = true;
                break;
            case IDLE:
                conversationStateTextView.setText("");
                this.meetingJoined = false;
                if (this.anonymousConversation != null) {
                    this.anonymousConversation.removeOnPropertyChangedCallback(this.conversationPropertyChangeListener);
                    this.anonymousConversation = null;
                }
                break;
            default:
        }

        // Refresh the UI
        this.updateUiState();

        if (meetingJoined) {
            this.navigateToConversationsActivity();
        }
    }

    /**
     * Helper method to show alerts.
     * @param message Alert message.
     */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.show();
    }

    /**
     * Callback implementation for listening for conversation property changes.
     */
    class ConversationPropertyChangeListener extends Observable.OnPropertyChangedCallback {
        /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            if (propertyId == Conversation.STATE_PROPERTY_ID) {
                updateConversationState();
            }
        }
    }
}
