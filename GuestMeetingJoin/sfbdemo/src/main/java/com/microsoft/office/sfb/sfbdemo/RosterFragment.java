/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Participant;
import com.microsoft.office.sfb.appsdk.SFBException;


/**
 * The ChatFragment class shows the list of chat items.
 * It uses a recycler view to show the list of chat items
 *
 * RecyclerView -----------> RosterAdapter     --------> ParticipantItemPresenter.
 *             (uses)  (RecyclerView.Adapter) (uses)  (RecyclerView.ViewHolder)
 *
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.ChatFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RosterFragment extends Fragment implements RosterAdapter.RosterAdapterEventListener{

    private RosterAdapter rosterAdapter = null;
    private RosterFragmentInteractionListener rosterFragmentInteractionListener = null;

    private static Conversation conversation = null;

    private Button selfHoldButton = null;
    private Button selfMuteButton = null;

    public RosterFragment() {
    }

    /**
     * Factory method to create a new instance of this fragment
     *
     * @return A new instance of fragment ChatFragment.
     */
    public static RosterFragment newInstance(Conversation conv) {
        RosterFragment fragment = new RosterFragment();
        conversation = conv;
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof RosterFragmentInteractionListener) {
            rosterFragmentInteractionListener = (RosterFragmentInteractionListener) activity;
        } else {
            //throw new RuntimeException(activity.toString()
            //        + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.participants_fragment_layout, container, false);

        this.updateSelfParticipantView(rootView);

        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(
                R.id.participants_list_recycler);

        this.rosterAdapter = new RosterAdapter(conversation);
        this.rosterAdapter.setAdapterEventListener(this);

        // Set the adapter for recyclerView
        recyclerView.setAdapter(this.rosterAdapter);

        // Set the layout manager.
        LinearLayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    /**
     * Show all the self-participant information.
     * @param rootView
     */
    private void updateSelfParticipantView(View rootView) {
        // Display the self participant data
        Participant self = conversation.getSelfParticipant();
        TextView selfParticipantDisplayName = (TextView)rootView.findViewById(
                R.id.selfParticipantDisplayId);
        String displayName = self.getPerson().getDisplayName();
        //@Todo: Bug: getDisplayName return URI for the participant.
        selfParticipantDisplayName.setText("Self");

        this.selfHoldButton = (Button)rootView.findViewById(R.id.selfParticipantHoldId);
        this.selfHoldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelfParticipantHoldButtonClicked(v);
            }
        });
        boolean isOnHold = conversation.getAudioService().isOnHold();
        this.updateSelfParticipantHoldButtonText(isOnHold);

        this.selfMuteButton = (Button)rootView.findViewById(R.id.selfParticipantMuteId);
        this.selfMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelfParticipantMuteButtonClicked(v);
            }
        });
        boolean isMuted = conversation.getSelfParticipant().getParticipantAudio().isMuted();
        this.updateSelfParticipantMuteButtonText(isMuted);
    }

    public void onSelfParticipantHoldButtonClicked(android.view.View view) {
        try {
            boolean isOnHold = conversation.getAudioService().isOnHold();
            if (conversation.getAudioService().canSetHold()) {
                conversation.getAudioService().setHold(!isOnHold);
                this.updateSelfParticipantHoldButtonText(!isOnHold);
            }
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    public void onSelfParticipantMuteButtonClicked(android.view.View view) {
        boolean isMuted = conversation.getSelfParticipant().getParticipantAudio().isMuted();
        if (conversation.getSelfParticipant().getParticipantAudio().canSetMuted()) {
            try {
                conversation.getSelfParticipant().getParticipantAudio().setMuted(!isMuted);
                this.updateSelfParticipantMuteButtonText(!isMuted);
            } catch (SFBException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSelfParticipantHoldButtonText(boolean isOnHold) {
        if (isOnHold) {
            this.selfHoldButton.setText("Resume");
        } else {
            this.selfHoldButton.setText("Hold");
        }
    }

    private void updateSelfParticipantMuteButtonText(boolean isMuted) {
        if (isMuted) {
            this.selfMuteButton.setText("Unmute");
        } else {
            this.selfMuteButton.setText("Mute");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        rosterFragmentInteractionListener = null;
    }

    @Override
    public void someMethod(int position) {

    }

    /**
     * Interface to communicate with the activity.
     */
    public interface RosterFragmentInteractionListener {
        void onRosterFragmentInteraction();
    }
}
