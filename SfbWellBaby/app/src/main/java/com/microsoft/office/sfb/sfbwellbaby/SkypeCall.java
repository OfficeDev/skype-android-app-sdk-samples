package com.microsoft.office.sfb.sfbwellbaby;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManager;
import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManagerImpl;

import butterknife.ButterKnife;

public class SkypeCall extends AppCompatActivity implements SkypeManager.SkypeConversationJoinCallback,
SkypeManager.SkypeConversationCanLeaveCallback,
SkypeManager.SkypeVideoReady{

    SkypeManagerImpl mSkypeManagerImpl;
    Conversation mAnonymousMeeting;
    SkypeCallFragment mCallFragment = null;

    ConversationPropertyChangeListener conversationPropertyChangeListener = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skype_call);
        ButterKnife.inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the singleton instance of the skype manager
        mSkypeManagerImpl = SkypeManagerImpl.getInstance(
                getApplicationContext(),
                this,this,this);

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

    @Override
    public void onSkypeConversationCanLeave(boolean canLeave) {
        // enable button to let caller leave

    }


    @Override
    public void onSkypeIncomingVideoReady() {

    }

    @Override
    public void onSkypeOutgoingVideoReady() {
        mSkypeManagerImpl.startOutgoingVideo();
    }
}
