package com.microsoft.office.sfb.sfbdemo;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.microsoft.office.sfb.appsdk.Conversation;

/**
 * Demo Application class
 */
public class SFBDemoApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
         super.attachBaseContext(base);
         MultiDex.install(this);
    }

    /**
     * Saving the anonymous conversation to be shared across activities.
     */
    private Conversation anonymousConversation = null;

    /**
     * Save the anonymous conversation.
     * @param conversation
     */
    public void setAnonymousConversation(Conversation conversation) {
        this.anonymousConversation = conversation;
    }

    /**
     * Get the anonymous conversation.
     * @return Conversation conversation.
     */
    public Conversation getAnonymousConversation() {
        return this.anonymousConversation;
    }

}
