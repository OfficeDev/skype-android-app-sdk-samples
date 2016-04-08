package com.microsoft.office.sfb.sfbwellbaby.SkypeAPI;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
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

    /**
     * Create or join a new (or existing) Conversation)
     *
     * @param meetingUri      the URI of the Conversation to create/join
     * @param userDisplayName the displayName of the joining participant
     * @return the newly created / joined Conversation
     * @throws SFBException if the meeting could not be created or joined
     */
    Conversation joinConversation(
            URI meetingUri,
            String userDisplayName
    ) throws SFBException;

    /**
     * Create or join a new (or existing) Conversation)
     *
     * @param meetingUri             the URI of the Conversation to create/join
     * @param userDisplayName        the displayName of the joining participant
     * @param propertyChangeListener property change listener for this Conversation
     * @return the newly created / joined Conversation
     * @throws SFBException
     */
    Conversation joinConversation(
            URI meetingUri,
            String userDisplayName,
            Observable.OnPropertyChangedCallback propertyChangeListener
    ) throws SFBException;

    /**
     * Create or join a new (or existing) Conversation)
     *
     * @param meetingUri                      the URI of the Conversation to create/join
     * @param userDisplayName                 the displayName of the joining participant
     * @param skypeConversationJoinedCallback listener for join events
     */
    void joinConversation(
            URI meetingUri,
            String userDisplayName,
            SkypeConversationJoinCallback skypeConversationJoinedCallback
    );

    /**
     * Create or join a new (or existing) Conversation)
     *
     * @param meetingUri                      the URI of the Conversation to create/join
     * @param userDisplayName                 the displayName of the joining participant
     * @param propertyChangeListener          property change listener for this Conversation
     * @param skypeConversationJoinedCallback listener for join events
     */
    void joinConversation(
            URI meetingUri,
            String userDisplayName,
            Observable.OnPropertyChangedCallback propertyChangeListener,
            SkypeConversationJoinCallback skypeConversationJoinedCallback
    );
}
