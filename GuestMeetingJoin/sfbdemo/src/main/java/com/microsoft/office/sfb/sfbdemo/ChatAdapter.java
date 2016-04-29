/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import java.util.Date;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.ConversationActivityItem;
import com.microsoft.office.sfb.appsdk.MessageActivityItem;
import com.microsoft.office.sfb.appsdk.ObservableList;
import com.microsoft.office.sfb.appsdk.ParticipantActivityItem;


/**
 * Adapter class for the RecyclerView.
 * The Adapter uses the ChatListItemPresenter (ViewHolder) to present the Chat item in the UI.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatItemPresenter> {

    private ObservableList<ConversationActivityItem> conversationActivityItemList= null;

    private Conversation conversation = null;

    /**
     * Listen for changes to the conversations list and notify the recyclerView via the adapter.
     */
    private ListChangeListener listChangeListener = null;

    private ChatAdapterEventListener adapterEventListener = null;

    public ChatAdapter(
            com.microsoft.office.sfb.appsdk.Conversation conversation) {
        this.conversation = conversation;
        this.conversationActivityItemList = conversation.getHistoryService().getConversationActivityItems();
        this.listChangeListener = new ListChangeListener(this);
        this.conversationActivityItemList.addOnListChangedCallback(listChangeListener);
    }

    /**
     * Called when RecyclerView needs a new {@link ConversationListItemPresenter} of the given type to represent
     * an item.
     *
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ChatItemPresenter, int)}. Since it will be re-used to display different
     * items in the data set, it is a good idea to cache references to sub views of the View to
     * avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ChatItemPresenter, int)
     */
    @Override
    public ChatItemPresenter onCreateViewHolder(ViewGroup parent, int viewType) {

        // CardView presents the ConversationActivity item.
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.chat_list_item, parent, false);
        return new ChatItemPresenter(cardView, this.conversation);
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
    public void onBindViewHolder(ChatItemPresenter holder, final int position) {

        CardView cv = holder.getCardView();
        TextView itemContentTextView = (TextView)cv.findViewById(R.id.itemContentViewId);
        TextView timeStampTextView = (TextView)cv.findViewById(R.id.timeStampViewId);
        // TextView itemStatusTextView = (TextView)cv.findViewById(R.id.itemStatusViewId);
        TextView remoteParticipantTextView = (TextView)cv.findViewById(R.id.remoteParticipantViewId);

        String itemContent = "";

        // Get the conversation activity item to display
        ConversationActivityItem activityItem = this.conversationActivityItemList.get(position);

        ConversationActivityItem.ActivityType activityType = activityItem.getType();
        switch (activityType) {
            case TEXTMESSAGE:
                String remoteParticipantName;
                MessageActivityItem messageActivityItem = (MessageActivityItem)activityItem;
                itemContent = messageActivityItem.getText();

                // Register callback for DisplayName and URI
                messageActivityItem.getSender().addOnPropertyChangedCallback(holder.getPropertyChangeListener());
                remoteParticipantName = messageActivityItem.getSender().getDisplayName();

                if (messageActivityItem.getDirection() == MessageActivityItem.MessageDirection.OUTGOING) {
                    // ToDo: DisplayName is returned as sip@anonymous.invalid. Fix.
                    remoteParticipantName = "";
               }
                // Set Remote participant Name
                remoteParticipantTextView.setText(remoteParticipantName);
                break;
            case PARTICIPANTJOINED:
            case PARTICIPANTLEFT:
                ParticipantActivityItem participantActivityItem = (ParticipantActivityItem)activityItem;

                // Register callback for DisplayName and URI
                participantActivityItem.getPerson().addOnPropertyChangedCallback(holder.getPropertyChangeListener());
                String activityString = (
                        activityType == ConversationActivityItem.ActivityType.PARTICIPANTJOINED) ? " Joined" : " Left";
                String participantName = participantActivityItem.getPerson().getDisplayName();
                itemContent = participantName + activityString;

                break;
            default:
        }
        // Set the timestamp
        Date itemTimeStamp = activityItem.getTimestamp();
        String timeString = android.text.format.DateFormat.getTimeFormat(cv.getContext()).format(itemTimeStamp);
        timeStampTextView.setText(timeString);

        // Set Item content
        itemContentTextView.setText(itemContent);

        // Set status
        // @Bug: Status does not resolve correctly.
        //itemStatusTextView.setText(activityItem.getStatus().toString());
    }

    /**
     * Set the adapter event listener.
     * @param listener ChatAdapterEventListener instance.
     */
    public void setAdapterEventListener(ChatAdapterEventListener listener) {
        this.adapterEventListener = listener;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return this.conversationActivityItemList.size();
    }

    public interface ChatAdapterEventListener {
        /**
         * Dummy method
         * @param position dummy var.
         */
        void someMethod(int position);
    }
}
