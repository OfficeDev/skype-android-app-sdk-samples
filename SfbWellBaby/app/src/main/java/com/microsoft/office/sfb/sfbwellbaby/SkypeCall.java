package com.microsoft.office.sfb.sfbwellbaby;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.net.Uri;
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


/**
 * SkypeCall activity hosts two fragments. The first fragment shows a progress indicator and is loaded
 * immediately after this activity is inflated. When the WaitForConnect fragment reports that it is
 * inflated, SkypeCall initiates the meeting join via SkypeManagerImpl.java. When SkypeManagerImpl
 * reports that the meeting is joined, SkypeCall removes the WaitForConnect fragment and replaces
 * it with the SkypeCallFragment. The SkypeCallFragment hosts the incoming video stream and a preview
 * of the outgoing stream.
 * SkypeCall finds the containing views in the SkypeCallFragment and provides those container views to
 * SkypeManagerImpl. SkypeManagerImpl sends the two video streams to the UI via those containers.
 */
public class SkypeCall extends AppCompatActivity
        implements SkypeManager.SkypeConversationJoinCallback, SkypeManager.SkypeVideoReady,
        SkypeCallFragment.OnFragmentInteractionListener,
        WaitForConnect.OnFragmentInteractionListener{

    SkypeManagerImpl mSkypeManagerImpl;
    Conversation mAnonymousMeeting;
    SkypeCallFragment mCallFragment = null;
    WaitForConnect mWaitForConnect = null;


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

        //Load the WaitForConnect fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        mWaitForConnect = WaitForConnect.newInstance("","");
        fragmentTransaction.add(
                R.id.fragment_container,
                mWaitForConnect,
                "wait");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * The WaitForConnect fragment interaction callback. This is invoked
     * when the WaitForConnect fragment is inflated.
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {

        //Start to join the meeting
        startToJoinMeeting();
    }

    /**
     * Invoked by SkypeManagerImpl when the meeting is joined.
     * @param conversation the newly joined / created Conversation
     * When joined, close the WaitForConnect fragment and add the
     * SkypeCallFragment
     */
    @Override
    public void onSkypeConversationJoinSuccess(Conversation conversation) {

        mCallFragment = SkypeCallFragment.newInstance(mSkypeManagerImpl);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();

        fragmentTransaction.remove(mWaitForConnect);
        fragmentTransaction.add(
                R.id.fragment_container,
                mCallFragment,
                "video");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mAnonymousMeeting = conversation;

    }

    /**
     * Invoked from SkypeCallFragment when inflated. Provides the TextureView for preview to the
     * SkypeManagerImpl
     * @param callView
     * @param newMeetingURI
     */
    @Override
    public void onFragmentInteraction(View callView, String newMeetingURI) {
        try{
            //Get the containers for preview video and incoming video
            @SuppressLint("WrongViewCast")
            TextureView textureView = (TextureView) callView.findViewById(R.id.selfParticipantVideoView);
            View participantVideoContainer = callView.findViewById(R.id.participantVideoLayoutId);

            //Set the preview video container
            mSkypeManagerImpl.setCallView(textureView);

            //Set the incoming video container and start the video
            mSkypeManagerImpl.startIncomingVideo(participantVideoContainer);

            //Prepare the video preview
            mSkypeManagerImpl.prepareOutgoingVideo();
        }
        catch (Exception ex){
            ex.printStackTrace();
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
     */
    private void startToJoinMeeting(){
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

                    null);
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
