package com.microsoft.office.sfb.sfbwellbaby;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManagerImpl;

import java.net.URI;
import java.net.URISyntaxException;

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

    SkypeManagerImpl mSkypeManagerImpl;
    SkypeCallFragment mCallFragment = null;
    FragmentManager mFragmentManager = null;
    Application mApplication;
    private static final String VIDEO_FRAGMENT_STACK_STATE = "videoFragment";
    Conversation mConversation;
    private ConversationPropertyChangeListener mConversationPropertyChangeListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentManager = getSupportFragmentManager();


        setContentView(R.layout.activity_skype_call);
        ButterKnife.inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        mConversation = startToJoinMeeting();
        mConversationPropertyChangeListener =
                new ConversationPropertyChangeListener();
        mConversation.addOnPropertyChangedCallback(mConversationPropertyChangeListener);
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
                mFragmentManager.popBackStack(
                        VIDEO_FRAGMENT_STACK_STATE,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                leaveSkypeCall();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Connect to an existing Skype for Business meeting with the URI from
     * shared preferences. Normally the URI is supplied to the mobile device
     * via some mechanism outside the scope of this sample.
     */
    private Conversation startToJoinMeeting() {
        URI meetingURI = null;
        Conversation conversation = null;
        try {
            meetingURI = new URI(getString(R.string.meeting_url));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {

            mApplication = Application.getInstance(this);
            conversation = mApplication
                    .joinMeetingAnonymously(
                            getString(
                                    R.string.fatherName), meetingURI);
        } catch (SFBException e) {
            e.printStackTrace();
        }
        return conversation;
    }


    @Override
    protected void onStop() {
        super.onStop();
        leaveSkypeCall();
    }

    private void leaveSkypeCall() {
        //If there is an active meeting and the user can leave then
        //leave the meeting before closing this activity

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.muteAudioMenuItem:
                break;
            case R.id.pauseVideoMenuItem:
                break;
        }

        return false;
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
                        Log.e("SkypeCall", "exception on meeting started");
                    }
                }
            }
        }
    }

}
