package com.microsoft.office.sfb.sfbwellbaby;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.TextureView;
import android.view.View;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManager;
import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManagerImpl;

import java.net.URI;
import java.net.URISyntaxException;

import butterknife.ButterKnife;

public class SkypeCall extends AppCompatActivity
        implements SkypeManager.SkypeConversationJoinCallback, SkypeManager.SkypeVideoReady,
        SkypeCallFragment.OnFragmentInteractionListener{

    SkypeManagerImpl mSkypeManagerImpl;
    Conversation mAnonymousMeeting;
    SkypeCallFragment mCallFragment = null;


    ConversationPropertyChangeListener conversationPropertyChangeListener = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the singleton instance of the skype manager
        mSkypeManagerImpl = SkypeManagerImpl.getInstance(
                getApplicationContext(),
                this,this);

        setContentView(R.layout.activity_skype_call);
        ButterKnife.inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mCallFragment = SkypeCallFragment.newInstance(mSkypeManagerImpl);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.add(R.id.fragment_container,mCallFragment, "video");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onSkypeConversationJoinSuccess(Conversation conversation) {
        mAnonymousMeeting = conversation;
        if (mAnonymousMeeting.getState() == Conversation.State.ESTABLISHED){
            View fragmentView = mCallFragment.getView();
            mSkypeManagerImpl.startIncomingVideo(
                    fragmentView
                            .findViewById(
                                    R.id.participantVideoLayoutId));
            mSkypeManagerImpl.prepareOutgoingVideo();

        }
    }

    @Override
    public void onSkypeConversationJoinFailure(SFBException ex) {
        //close call fragment

    }


    /**
     * Connect to an existing Skype for Business meeting with the URI from
     * shared preferences. Normally the URI is supplied to the mobile device
     * via some mechanism outside the scope of this sample.
     * @param callView The fragment that hosts the incoming and outgoing video
     */
    private void startConversation(View callView){
        SharedPreferences settings = getSharedPreferences(getString(R.string.meetingURIKey), 0);
        String meetingURIString = settings.getString(getString(R.string.meetingURIKey), "");
        URI meetingURI = null;
        try {
            meetingURI = new URI(meetingURIString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            mSkypeManagerImpl.joinConversation(
                    meetingURI,
                    getString(R.string.fatherName),

                    (TextureView) callView
                            .findViewById(
                                    R.id.selfParticipantVideoView));
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSkypeIncomingVideoReady() {

    }

    /**
     * The fragment TextureView that displays the preview is ready.
     * Start the outgoing video.
     */
    @Override
    public void onSkypeOutgoingVideoReady() {
        mSkypeManagerImpl.startOutgoingVideo();
    }

    @Override
    public void onFragmentInteraction(View callView, String newMeetingURI) {
        startConversation(callView);
    }
    @Override
    protected void onStop() {
        super.onStop();

        //If there is an active meeting and the user can leave then
        //leave the meeting before closing this activity
        if (mAnonymousMeeting != null && mAnonymousMeeting.canLeave())
            try {
                mAnonymousMeeting.leave();
            } catch (SFBException e) {
                e.printStackTrace();
            }
    }
}
