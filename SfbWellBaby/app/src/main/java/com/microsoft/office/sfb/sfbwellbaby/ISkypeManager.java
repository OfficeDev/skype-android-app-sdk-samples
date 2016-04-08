package com.microsoft.office.sfb.sfbwellbaby;

import android.view.View;

import java.net.URI;

/**
 * Created by johnau on 4/7/2016.
 */
public interface ISkypeManager {
    public Error joinMeeting(
            URI meetingURL,
            String displayName
            ) throws Throwable;

    public void leaveMeetingWithError(Error error);

    public boolean stopStartOutgoingAudio();

    public Error prepareOutgoingVideo(View videoContainer,
                                      int surfaceViewId);

    public void startOutgoingVideo();
    public void stopOutgoingVideo();
    public boolean canGetOrCreateConversation();
}
