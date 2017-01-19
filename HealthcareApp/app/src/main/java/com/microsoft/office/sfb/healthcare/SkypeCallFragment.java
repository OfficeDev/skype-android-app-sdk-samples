/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */

package com.microsoft.office.sfb.healthcare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.microsoft.media.MMVRSurfaceView;
import com.microsoft.office.sfb.appsdk.AudioService;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.MessageActivityItem;
import com.microsoft.office.sfb.appsdk.SFBException;


/**
 * A placeholder fragment containing a simple view.
 */
public class SkypeCallFragment extends Fragment
        implements ConversationHelper.ConversationCallback {

    public Button mEndCallButton;
    public Button mMuteAudioButton;
    private OnFragmentInteractionListener mListener;
    private static Conversation mConversation;
    private static DevicesManager mDevicesManager;

    //// TODO: 1/13/2017 give hint re: location of the source file
	protected ConversationHelper mConversationHelper;
    private MMVRSurfaceView mParticipantVideoSurfaceView;
    private TextureView mPreviewVideoTextureView;
    private boolean mTryStartVideo = false;
    View mRootView;

    @SuppressLint("ValidFragment")
    public SkypeCallFragment() {
    }

    /**
     * Create the Video fragment.
     *
     * @return A new instance of fragment VideoFragment.
     */
    public static SkypeCallFragment newInstance(
            Conversation conversation,
            DevicesManager devicesManager) {
        mConversation = conversation;
        mDevicesManager = devicesManager;
        SkypeCallFragment fragment = new SkypeCallFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_skype_call, container, false);
        mEndCallButton = (Button) mRootView.findViewById(R.id.endCallButton);
        mEndCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConversation.canLeave())
                    try {
                        mConversation.leave();
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
            }
        });

        mMuteAudioButton = (Button) mRootView.findViewById(R.id.muteAudioButton);
        mMuteAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTryStartVideo == true){
                    tryStartVideo();
                }
                mConversationHelper.toggleMute();
            }
        });
        Log.i(
                "SkypeCallFragment",
                "onCreateView ");
        return mRootView;
    }

        @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreviewVideoTextureView = (TextureView) mRootView.findViewById(
                R.id.selfParticipantVideoView);
        RelativeLayout participantVideoLayout = (RelativeLayout) mRootView.findViewById(
                R.id.participantVideoLayoutId);
		mParticipantVideoSurfaceView = (MMVRSurfaceView) mRootView.findViewById(R.id.mmvrSurfaceViewId);
        mListener.onFragmentInteraction(mRootView
                , getActivity()
                        .getString(
                                R.string.callFragmentInflated));
        tryStartVideo();


    }

    private void tryStartVideo(){
            //Initialize the conversation helper with the established conversation,
            //the SfB App SDK devices manager, the outgoing video TextureView,
            //The view container for the incoming video, and a conversation helper
            //callback.
        if (mConversationHelper == null){
            mConversationHelper = new ConversationHelper(
                    mConversation,
                    mDevicesManager,
                    mPreviewVideoTextureView,
                    mParticipantVideoSurfaceView,
                    this);
            Log.i(
                    "SkypeCallFragment",
                    "onViewCreated");

        }
        if (mParticipantVideoSurfaceView.isActivated()){
            mTryStartVideo = false;

        } else {
            mParticipantVideoSurfaceView.setActivated(true);
            mPreviewVideoTextureView.setActivated(true);
            mTryStartVideo = true;
        }
            mConversationHelper.startOutgoingVideo();
            mConversationHelper.startIncomingVideo();


    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    /**
     * Used to interact with parent activity
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View rootView, String fragmentAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mListener != null) {

            //TODO stop the call and then leave
            mListener.onFragmentInteraction(
                    mRootView,
                    getActivity().
                            getString(R.string.leaveCall));
            mListener = null;
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    public void onDetach() {
        super.onDetach();
    }


    /**
     * Invoked when the state of the established conversation changes from
     * ESTABLISHED to IDle. State change happens when the call ends.
     * @param state
     */
    @Override
    public void onConversationStateChanged(Conversation.State state) {
        Log.i(
                "SkypeCallFragment",
                "onConversationStateChanged "
                        + String.valueOf(state));

        if (state == Conversation.State.IDLE) {
            if (mListener != null) {
                mListener.onFragmentInteraction(
                        mRootView,
                        getActivity().
                                getString(R.string.leaveCall));
                mListener = null;
            }
        }
    }

    @Override
    public void onCanSendMessage(boolean canSendMessage) {
        Log.i(
                "SkypeCallFragment",
                "onCanSendMessage "
                        + String.valueOf(canSendMessage));
    }

    @Override
    public void onMessageReceived(MessageActivityItem messageActivityItem) {

    }

    @Override
    public void onSelfAudioMuteChanged(final AudioService.MuteState newMuteState) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (newMuteState == AudioService.MuteState.MUTED) {
                        mMuteAudioButton.setText("Unmute");
                    } else {
                        mMuteAudioButton.setText("Mute");
                    }
                }
            });
        } catch (Exception e) {
            Log.e("SkypeCall", "exception on meeting started");
        }

    }


    /**
     * Called when the video service on the established conversation can be
     * started. Use the callback to start video.
     * @param canStartVideoService
     *
     * This seems to be called when the conversation is ended, not when it starts.
     */
    @Override
    public void onCanStartVideoServiceChanged(boolean canStartVideoService) {
        Log.i(
                "SkypeCallFragment",
                "onCanStartVideoServiceChanged "
                        + String.valueOf(canStartVideoService));

        if (canStartVideoService == true) {
            mConversationHelper.startOutgoingVideo();
            mConversationHelper.startIncomingVideo();
        }
    }


    /**
     * Called when the video service pause state changes
     * @param canSetPausedVideoService
     */
    @Override
    public void onCanSetPausedVideoServiceChanged(boolean canSetPausedVideoService) {

        //HACK!!
        mConversationHelper.startIncomingVideo();

        if (canSetPausedVideoService)
            mConversationHelper.ensureVideoIsStartedAndRunning();
    }

    @Override
    public void onCanSetActiveCameraChanged(boolean canSetActiveCamera) {
        Log.i(
                "SkypeCallFragment",
                "onCanSetActiveCameraChanged "
                        + String.valueOf(canSetActiveCamera));
        if (mListener != null){
            mListener.onFragmentInteraction(mRootView, getString(R.string.canToggleCamera));
        }

    }
}
