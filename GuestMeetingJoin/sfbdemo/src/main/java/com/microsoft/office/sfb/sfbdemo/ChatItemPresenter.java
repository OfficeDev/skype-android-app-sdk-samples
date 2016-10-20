/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.ConversationActivityItem;
import com.microsoft.office.sfb.appsdk.MessageActivityItem;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.Person;

/**
 * The ChatItem presenter presents the ConversationActivityItem in a CardView
 */
public class ChatItemPresenter extends RecyclerView.ViewHolder{

    private CardView cardView = null;
    private Conversation conversation = null;

    /**
     * Listen for property changes on the chat item.
     * Helpful for status on outgoing text messages.
     */
    private ChatItemPropertyChangeListener propertyChangeListener = null;

    public ChatItemPresenter(CardView view, Conversation conversation) {
        super(view);
        this.cardView = view;
        this.conversation = conversation;
        this.propertyChangeListener = new ChatItemPropertyChangeListener(this);
    }

    public CardView getCardView() {
        return this.cardView;
    }

    /**
     * Set the item status.
     * @param subject
     */
    public void setStatus(String subject) {
        TextView messageStatusTextView = (TextView)this.cardView.findViewById(R.id.itemStatusViewId);
        //@Todo Reliability Fix
        //messageStatusTextView.setText(subject);
    }

    /**
     * Set the timestamp.
     * @param state
     */
    public void setTimestamp(String state) {
        TextView timestampTextView = (TextView)this.cardView.findViewById(R.id.timeStampViewId);
        timestampTextView.setText(state);
    }

    public void setDisplayName(String displayName) {
        TextView remoteParticipantTextView = (TextView)this.cardView.findViewById(
                R.id.remoteParticipantViewId);
        remoteParticipantTextView.setText(displayName);

    }
    /**
     * Get the property change listener
     * @return
     */
    public ChatItemPropertyChangeListener getPropertyChangeListener() {
        return this.propertyChangeListener;
    }

    /**
     * PropertyChangeListener class for listening to conversationItem property changes.
     */
    private class ChatItemPropertyChangeListener extends Observable.OnPropertyChangedCallback {

        private ChatItemPresenter chatItemPresenter = null;

        public ChatItemPropertyChangeListener(ChatItemPresenter chatItemPresenter) {
            this.chatItemPresenter = chatItemPresenter;
        }

        /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            if (ConversationActivityItem.class.isInstance(sender)) {
                ConversationActivityItem conversationActivityItem = (ConversationActivityItem) sender;
                switch (propertyId) {
                    case ConversationActivityItem.TIMESTAMP_CHANGED_PROPERTY_ID:
                        this.chatItemPresenter.setTimestamp(conversationActivityItem.getTimestamp().toString());
                        break;
                    default:
                }
            }

            if (MessageActivityItem.class.isInstance(sender)) {
                MessageActivityItem messageActivityItem = (MessageActivityItem) sender;
                switch (propertyId) {
                    case MessageActivityItem.STATUS_CHANGED_PROPERTY_ID:
                        this.chatItemPresenter.setStatus(messageActivityItem.getStatus().toString());
                        break;
                    default:
                }
            }

            if (Person.class.isInstance(sender)) {
                Person person = (Person)sender;
                switch (propertyId) {
                    case Person.DISPLAYNAME_PROPERTY_ID:
                        this.chatItemPresenter.setDisplayName(person.getDisplayName());
                        break;
                    case Person.SIPURI_PROPERTY_ID:
                        break;
                    default:
                }
            }
        }
    }


}
