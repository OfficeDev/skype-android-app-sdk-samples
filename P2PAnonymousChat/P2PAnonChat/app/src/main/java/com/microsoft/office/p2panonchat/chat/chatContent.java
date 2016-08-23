package com.microsoft.office.p2panonchat.chat;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class chatContent {


    /**
     * An item representing a piece of content.
     */
    public static class ChatItem {
        public final String id;
        public final String content;

        public ChatItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
