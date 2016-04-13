package com.microsoft.office.sfb.sfbwellbaby;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManager;
import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManagerImpl;

import java.net.URI;
import java.net.URISyntaxException;

import butterknife.ButterKnife;

public class SkypeCall extends AppCompatActivity implements SkypeManager.SkypeConversationJoinCallback {

    SkypeManagerImpl mSkypeManagerImpl;
    Conversation mAnonymousMeeting;

    ConversationPropertyChangeListener conversationPropertyChangeListener = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skype_call);
        ButterKnife.inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences settings = getSharedPreferences(getString(R.string.meetingURIKey), 0);
        String meetingURIString = settings.getString(getString(R.string.meetingURIKey), "");
        URI meetingURI = null;
        try {
            meetingURI = new URI(meetingURIString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //Get the singleton instance of the skype manager
        mSkypeManagerImpl = SkypeManagerImpl.getInstance(this);

        //tip up a conversation property changed listener
        conversationPropertyChangeListener = new ConversationPropertyChangeListener(
                this,
                mAnonymousMeeting);

        //Join the conversation
        mSkypeManagerImpl.joinConversation(
                meetingURI,
                getString(R.string.fatherName),
                conversationPropertyChangeListener
                );


    }

    @Override
    public void onSkypeConversationJoinSuccess(Conversation conversation) {
        mAnonymousMeeting = conversation;
        if (mAnonymousMeeting.getState() == Conversation.State.ESTABLISHED){
 // create the video fragment and listen for fragment events
            SkypeCallFragment callFragment = SkypeCallFragment.newInstance(
                    mAnonymousMeeting,mSkypeManagerImpl
                            .getSkypeApplication()
                            .getDevicesManager());
        }
    }

    @Override
    public void onSkypeConversationJoinFailure(SFBException ex) {

    }

    /**
     * Callback implementation for listening for conversation property changes.
     */
    class ConversationPropertyChangeListener extends Observable.OnPropertyChangedCallback {

        private SkypeCall mainActivity = null;

        public ConversationPropertyChangeListener(Activity activity, Conversation conversation) {
            this.mainActivity = (SkypeCall) activity;
        }

        /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            if (propertyId == Conversation.STATE_PROPERTY_ID) {
            }
        }

    }
}
