package com.microsoft.office.sfb.sfbwellbaby.SkypeAPI;

import android.view.TextureView;
import android.view.View;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.URI;

/**
 * Describes how join/create {@link Conversation} Objects
 */
public interface SkypeManager {

    /**
     * Obeserver to watch for when a Conversation is created / joined
     */
    interface SkypeConversationJoinCallback {

        /**
         * Called after a Conversation has been successfully joined
         *
         * @param conversation the newly joined / created Conversation
         */
        void onSkypeConversationJoinSuccess(Conversation conversation);

        /**
         * Called after a Conversation has been unsuccessfully joined, due to an error
         *
         * @param ex the exception thrown by the SFB subsystem
         */
        void onSkypeConversationJoinFailure(SFBException ex);
    }


    interface SkypeVideoReady {
        void onSkypeIncomingVideoReady();
        void onSkypeOutgoingVideoReady();
    }

    /**
     * Joins a meeting with provided meeting URI and shows video
     * streams on provided surface views
     * @param meetingURI
     * @param displayName
     * @throws  SFBException
     */
    void joinConversation(
            URI meetingURI,
            String displayName
    )throws SFBException;

    void setCallView(View callView);
    void prepareOutgoingVideo();

    void startOutgoingVideo();
    void stopOutgoingVideo();
    void startIncomingVideo(
            View participantVideoLayout);





}
