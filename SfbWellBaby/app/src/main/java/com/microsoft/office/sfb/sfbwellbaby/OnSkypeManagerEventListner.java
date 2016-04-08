package com.microsoft.office.sfb.sfbwellbaby;

import com.microsoft.office.sfb.appsdk.Conversation;

/**
 * Created by johnau on 4/7/2016.
 */

public interface OnSkypeManagerEventListner {
    class SkypeMeetingEvent {
        private boolean mCanJoinOrLeaveMeeting = false;
        public boolean canJoinOrLeaveMeeting() {
            return mCanJoinOrLeaveMeeting;
        }
        public void setCanJoinOrLeaveMeeting(boolean value){
            mCanJoinOrLeaveMeeting = value;
        }
    }
    class SkypeConversationsEvent{
        private Conversation.State mConversationState;
        public Conversation.State conversationState(){
            return mConversationState;
        }
        public void setConversationState(Conversation.State state){
            mConversationState = state;
        }
    }
    public void onSkypeMeetingLeaveAvailabilityChanged(SkypeMeetingEvent event);
    public void onSkypeMeetingJoinAvailabilityChanged(SkypeMeetingEvent event);
    public void onSkypeConversationStateChanged(SkypeConversationsEvent event);
    public void onSkypeIncomingVideoReady();
    public void onSkypeOutgoingVideoReady();
}
