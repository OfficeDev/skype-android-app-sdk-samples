/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.Participant;
import com.microsoft.office.sfb.appsdk.ParticipantAudio;
import com.microsoft.office.sfb.appsdk.ParticipantService;
import com.microsoft.office.sfb.appsdk.SFBException;

/**
 * The ParticipantItemPresenter presents the Participant information in a CardView.
 */
public class ParticipantItemPresenter extends RecyclerView.ViewHolder{

    private Conversation conversation = null;

    private Participant participant = null;
    private ParticipantAudio participantAudio = null;

    /**
     * Listen for property changes on the participant.
     * The UI will be updated if the role of the participant changes. E.g. if the participant is
     * promoted to a LEADER, the user can mute / un-mute other participants.
     */
    private ParticipantItemPropertyChangeListener propertyChangeListener = null;

    private CardView cardView = null;
    private TextView participantDisplayNameTextView = null;
    private TextView participantOnHoldTextView = null;
    private Button participantMuteButton = null;

    boolean isLocalParticipantLeader = false;

    public ParticipantItemPresenter(CardView view, Conversation conversation) {
        super(view);
        this.cardView = view;
        this.conversation = conversation;
        this.propertyChangeListener = new ParticipantItemPropertyChangeListener();
        this.participant = null;

        this.participantDisplayNameTextView = (TextView)this.cardView.findViewById(
                R.id.participantDisplayId);

        this.participantOnHoldTextView = (TextView)this.cardView.findViewById(
                R.id.participantHoldId);

        this.participantMuteButton = (Button)this.cardView.findViewById(R.id.participantMuteId);
        this.participantMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onParticipantMuteButtonClicked(v);
            }
        });


        this.conversation.getSelfParticipant().addOnPropertyChangedCallback(
                this.propertyChangeListener);
        this.setLocalParticipantLeader();
    }

    public CardView getCardView() {
        return this.cardView;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
        this.participant.addOnPropertyChangedCallback(this.propertyChangeListener);
        this.participantAudio = this.participant.getParticipantAudio();
        this.participantAudio.addOnPropertyChangedCallback(this.propertyChangeListener);
    }

    public void setDisplayName() {
        this.participantDisplayNameTextView.setText(this.participant.getPerson().getDisplayName());
    }

    public void updateView() {
        this.setDisplayName();

        // Update if the participant is on hold.
        boolean isOnHold = this.participant.getParticipantAudio().isOnHold();
        this.updateParticipantViewOnHold(isOnHold);

        // Update Mute/Un-mute button state.
        this.updateParticipantViewOnMute();

    }

    public void onParticipantMuteButtonClicked(android.view.View view) {
        ParticipantAudio participantAudio = this.participant.getParticipantAudio();
        boolean isMuted = participantAudio.isMuted();
        if (participantAudio.canSetMuted()) {
            try {
                participantAudio.setMuted(!isMuted);
            } catch (SFBException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateParticipantViewOnMute() {
        boolean isMuted = participantAudio.isMuted();
        this.updateParticipantViewOnMute(isMuted);
        this.participantMuteButton.setEnabled(this.isLocalParticipantLeader);
    }

    private void updateParticipantViewOnMute(boolean isMuted) {
        if (isMuted) {
            this.participantMuteButton.setText("Unmute");
        } else {
            this.participantMuteButton.setText("Mute");
        }
    }

    private void updateParticipantMuteButtonState(boolean state) {
        if (this.isLocalParticipantLeader) {
            this.participantMuteButton.setEnabled(state);
        } else {
            this.participantMuteButton.setEnabled(false);
        }
    }

    public void updateParticipantViewOnHold(boolean isOnHold) {
        if (isOnHold) {
            this.participantOnHoldTextView.setVisibility(View.VISIBLE);
            this.updateParticipantMuteButtonState(false);
        } else {
            this.participantOnHoldTextView.setVisibility(View.INVISIBLE);
            this.updateParticipantMuteButtonState(true);
        }
    }

    public void setLocalParticipantLeader() {
        this.isLocalParticipantLeader = (this.conversation.getSelfParticipant().getRole() ==
                Participant.Role.LEADER);
    }


    /**
     * Get the property change listener
     * @return
     */
    public ParticipantItemPropertyChangeListener getPropertyChangeListener() {
        return this.propertyChangeListener;
    }

    /**
     * PropertyChangeListener class for listening to conversationItem property changes.
     */
    private class ParticipantItemPropertyChangeListener extends Observable.OnPropertyChangedCallback {
        /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {

            // Event handler for Local participant role change.
            // Only leaders can Mute / Unmute other participants in the conversation.
            if (Participant.class.isInstance(sender)) {
                switch (propertyId) {
                    case Participant.ROLE_CHANGED_PROPERTY_ID:
                        setLocalParticipantLeader();
                        updateView();
                        break;
                }
            }

            // Event handler for Participant Audio events.
            if (ParticipantAudio.class.isInstance(sender)) {
                ParticipantAudio participantAudio = (ParticipantAudio)sender;
                switch (propertyId) {
                    case ParticipantAudio.PARTICIPANT_IS_MUTED_PROPERTY_ID:
                        updateParticipantViewOnMute();
                        break;
                    case ParticipantAudio.PARTICIPANT_IS_ON_HOLD_PROPERTY_ID:
                    case ParticipantService.PARTICIPANT_SERVICE_STATE_PROPERTY_ID:
                        updateParticipantViewOnHold(participantAudio.isOnHold());
                        break;



                }
            }
        }
    }


}
