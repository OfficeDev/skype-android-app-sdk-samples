package com.microsoft.office.sfb.sfbwellbaby;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.ConfigurationManager;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.ConversationsManager;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.URI;

/**
 * Created by johnau on 4/7/2016.
 */

public class SkypeManager implements   ISkypeManager{

    Application mApplication = null;
    ConversationsManager mConversationsManager = null;
    ConfigurationManager mConfigurationManager = null;
    DevicesManager mDevicesManager = null;
    protected ConversationPropertyChangeListener mConversationPropertyChangeListener = null;
    Conversation mAnonymousConversation = null;
    Activity mActivity;
    boolean mMeetingJoined = false;

    public SkypeManager(Activity activity) throws Throwable {
        if (activity == null)
            throw new Throwable("Activity is null");
        mActivity = activity;
        mApplication = Application.getInstance(activity.getApplication().getApplicationContext());
        mConversationsManager = mApplication.getConversationsManager();
        mConfigurationManager = mApplication.getConfigurationManager();
        mDevicesManager = mApplication.getDevicesManager();
    }
    @Override
    public Error joinMeeting(
            URI meetingURL,
            String displayName) throws Throwable {
        if (meetingURL == null )
            throw new Throwable("Meeting URI is null");

        if (displayName.isEmpty())
            displayName = "Default user";

        Error error = null;
        InputMethodHelper.hideSoftKeyBoard(mActivity.getApplication().getApplicationContext(),
                mActivity.findViewById(R.id.participantVideoLayoutId).getWindowToken());

        if (mConversationsManager.getConversations().size() > 0){

        }
        if (mMeetingJoined == true) {
            // Leave the meeting.
            try {

                mAnonymousConversation.leave();
                mMeetingJoined = false;
                //this.updateUiState();

            } catch (SFBException e) {
                e.printStackTrace();
            }
        } else {
            //Join the meeting.

            mConfigurationManager.setDisplayName(mActivity.getString(R.string.localParticipantName));

            SharedPreferences settings = mActivity.getPreferences(0);


            //Get meeting URI from settings
                URI meetingUri = URI.create(settings.getString("meetingurl","https://meet.lync.com/microsoft/johnau/H7TW81MG"));

            // Join meeting and monitor conversation state to determine meeting join completion.
            try {

                // Set the default device to Speaker
                mDevicesManager.setActiveEndpoint(DevicesManager.Endpoint.LOUDSPEAKER);

                if (mConversationsManager.canGetOrCreateConversationMeetingByUri()) {
                    mAnonymousConversation = mConversationsManager.getOrCreateConversationMeetingByUri(meetingUri);

                    // Conversation begins in Idle state. It will move from Idle->Establishing->InLobby/Established
                    // depending on meeting configuration.
                    // We will monitor property change notifications for State property.
                    // Once the conversation is Established, we will move to the next activity.
                    mConversationPropertyChangeListener = new ConversationPropertyChangeListener(mAnonymousConversation);
                    mAnonymousConversation.addOnPropertyChangedCallback(mConversationPropertyChangeListener);
                } else {
                    error = new Error("Cannot join meeting. canGetOrCreateConversationMeetingByUri false");
                }
            } catch (SFBException e) {
                e.printStackTrace();
                error = new Error(e.getMessage());
            }
        }
        return error;
    }

    private Error joinMeeting_(URI meetingURI, String displayName){

        return null;
    }
    @Override
    public void leaveMeetingWithError(Error error) {

    }

    @Override
    public boolean stopStartOutgoingAudio() {
        return false;
    }

    @Override
    public Error prepareOutgoingVideo(View videoContainer,
                                      int surfaceViewId) {
        return null;
    }

    @Override
    public void startOutgoingVideo() {

    }

    @Override
    public void stopOutgoingVideo() {

    }

    @Override
    public boolean canGetOrCreateConversation() {

        return mConversationsManager.canGetOrCreateConversationMeetingByUri();
    }
}
