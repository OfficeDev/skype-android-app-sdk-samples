/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.ObservableList;
import com.microsoft.office.sfb.appsdk.Participant;

/**
 * Adapter class for the RecyclerView.
 * The Adapter uses the ParticipantItemPresenter (ViewHolder) to present the Participant in the UI.
 */
public class RosterAdapter extends RecyclerView.Adapter<ParticipantItemPresenter> {

    private ObservableList<Participant> participantItemList= null;
    private Conversation conversation = null;

    /**
     * Listen for changes to the participant list and notify the recyclerView via the adapter.
     */
    private ListChangeListener listChangeListener = null;

    private RosterAdapterEventListener adapterEventListener = null;

    public RosterAdapter(
            com.microsoft.office.sfb.appsdk.Conversation conversation) {
        this.conversation = conversation;
        this.participantItemList = this.conversation.getRemoteParticipants();
        this.listChangeListener = new ListChangeListener(this);
        this.participantItemList.addOnListChangedCallback(listChangeListener);
    }

    /**
     * Called when RecyclerView needs a new {@link ParticipantItemPresenter} of the given type to
     * represent an item.
     *
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ParticipantItemPresenter, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of the
     * View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ParticipantItemPresenter, int)
     */
    @Override
    public ParticipantItemPresenter onCreateViewHolder(ViewGroup parent, int viewType) {

        // CardView presents the Participant information.
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.participants_list_item, parent, false);
        return new ParticipantItemPresenter(cardView, this.conversation);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method
     * should update the contents of the {@link ConversationListItemPresenter#itemView} to reflect the item at
     * the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ParticipantItemPresenter holder, final int position) {
        Participant participant = this.participantItemList.get(position);
        holder.setParticipant(participant);
        holder.updateView();
    }

    /**
     * Set the adapter event listener.
     * @param listener Listener
     */
    public void setAdapterEventListener(RosterAdapterEventListener listener) {
        this.adapterEventListener = listener;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return this.participantItemList.size();
    }

    public interface RosterAdapterEventListener {
        /**
         * Dummy method
         * @param position dummy var.
         */
        void someMethod(int position);
    }
}
