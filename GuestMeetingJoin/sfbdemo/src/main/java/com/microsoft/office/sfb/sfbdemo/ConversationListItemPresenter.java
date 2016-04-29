/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;

/**
 * The ConversationListItemPresenter presents the Conversation.
 */
public class ConversationListItemPresenter extends RecyclerView.ViewHolder{

    /**
     * Conversation presenter
     */
    private CardView cardView = null;

    /**
     * Listen for property changes on the conversation.
     */
    private PropertyChangeListener propertyChangeListener = null;


    public ConversationListItemPresenter(CardView view) {
        super(view);
        this.cardView = view;
        this.propertyChangeListener = new PropertyChangeListener();
    }

    /**
     * Get the card view
     * @return
     */
    public CardView getCardView() {
        return this.cardView;
    }

    /**
     * Set the subject
     * @param subject
     */
    public void setSubject(String subject) {
        TextView subjectTextView = (TextView)this.cardView.findViewById(R.id.subjectTextViewId);
        subjectTextView.setText(subject);
    }

    /**
     * Set the conversation state.
     * @param state
     */
    public void setState(String state) {
        TextView stateTextView = (TextView)this.cardView.findViewById(R.id.statusTextViewId);
        stateTextView.setText(state);
    }

    /**
     * Returns the Property change listener instance.
     * @return
     */
    public PropertyChangeListener getPropertyChangeListener() {
        return this.propertyChangeListener;
    }

    /**
     * Implements the PropertyChange listener for the Conversation object.
     */
    private class PropertyChangeListener extends Observable.OnPropertyChangedCallback {
        /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            Conversation conversation = (Conversation)sender;
            switch (propertyId) {
                case Conversation.SUBJECT_PROPERTY_ID:
                    setSubject(conversation.getSubject());
                    break;
                case Conversation.STATE_PROPERTY_ID:
                    setState(conversation.getState().toString());
                    break;
                default:
            }
        }
    }
}