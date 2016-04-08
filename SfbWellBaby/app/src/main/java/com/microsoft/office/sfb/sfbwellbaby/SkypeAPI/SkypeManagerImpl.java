package com.microsoft.office.sfb.sfbwellbaby.SkypeAPI;

import android.content.Context;

import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.URI;

/**
 *
 */
public class SkypeManagerImpl implements SkypeManager {

    //
    // statics
    //
    private static SkypeManagerImpl sSkypeManager;

    /**
     * Create or return a new instance
     *
     * @param context Your application's context
     * @return a new or existing instance of SkypeManager2
     */
    public static synchronized SkypeManagerImpl getInstance(Context context) {
        if (null != sSkypeManager) { // initialize a new instance of singleton
            sSkypeManager = new SkypeManagerImpl(
                    Application.getInstance(context)
            );
        }
        return sSkypeManager;
    }

    //
    // Instance fields
    //
    private final Application mSkypeApplication;

    // constructor
    private SkypeManagerImpl(Application skypeApplication) {
        mSkypeApplication = skypeApplication;
    }

    /**
     * Returns a reference to the Skype Application framework
     *
     * @return the Skype application
     */
    public Application getSkypeApplication() {
        return mSkypeApplication;
    }

    @Override
    public Conversation joinConversation(
            URI meetingUri,
            String userDisplayName
    ) throws SFBException {
        setDisplayName(userDisplayName); // set our name
        return getConversation(meetingUri); // if it already exists, we'll join otherwise create it
    }

    @Override
    public Conversation joinConversation(
            URI meetingUri,
            String userDisplayName,
            Observable.OnPropertyChangedCallback propertyChangeListener
    ) throws SFBException {
        // create/join
        final Conversation joinedConversation
                = joinConversation(
                meetingUri,
                userDisplayName
        );

        // add the oberserver
        joinedConversation
                .addOnPropertyChangedCallback(
                        propertyChangeListener
                );

        return joinedConversation;
    }

    @Override
    public void joinConversation(
            URI meetingUri,
            String userDisplayName,
            SkypeConversationJoinCallback skypeConversationJoinedCallback
    ) {
        try {
            // create or join
            final Conversation joinedConversation =
                    joinConversation(
                            meetingUri,
                            userDisplayName
                    );

            // pass to callback
            skypeConversationJoinedCallback
                    .onSkypeConversationJoinSuccess(
                            joinedConversation
                    );
        } catch (SFBException e) {
            skypeConversationJoinedCallback
                    .onSkypeConversationJoinFailure(e);
        }
    }

    @Override
    public void joinConversation(
            URI meetingUri,
            String userDisplayName,
            Observable.OnPropertyChangedCallback propertyChangeListener,
            SkypeConversationJoinCallback skypeConversationJoinedCallback
    ) {
        try {
            // create or join
            final Conversation joinedConversation =
                    joinConversation(
                            meetingUri,
                            userDisplayName,
                            propertyChangeListener
                    );

            // pass to callback
            skypeConversationJoinedCallback
                    .onSkypeConversationJoinSuccess(
                            joinedConversation
                    );
        } catch (SFBException e) {
            skypeConversationJoinedCallback
                    .onSkypeConversationJoinFailure(e);
        }
    }

    //
    // private methods
    //

    private Conversation getConversation(URI meetingUri) throws SFBException {
        return mSkypeApplication
                .getConversationsManager()
                .getOrCreateConversationMeetingByUri(meetingUri);
    }

    private void setDisplayName(String userDisplayName) {
        // FIXME why do I set my name over here, so that it shows up in a Conversation?
        // strange....
        mSkypeApplication
                .getConfigurationManager()
                .setDisplayName(userDisplayName);
    }
}
