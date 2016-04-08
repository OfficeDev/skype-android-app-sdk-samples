package com.microsoft.office.sfb.sfbwellbaby;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;

/**
 * Callback implementation for listening for conversation property changes.
 */

public class ConversationPropertyChangeListener extends Observable.OnPropertyChangedCallback {


    public ConversationPropertyChangeListener(Conversation conversation) {
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
