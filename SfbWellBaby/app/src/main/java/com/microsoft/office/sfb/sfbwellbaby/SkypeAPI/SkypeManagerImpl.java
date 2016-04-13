package com.microsoft.office.sfb.sfbwellbaby.SkypeAPI;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import com.microsoft.media.MMVRSurfaceView;
import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.Camera;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.appsdk.VideoService;

import java.net.URI;
import java.util.ArrayList;

/**
 *
 */
public class SkypeManagerImpl implements SkypeManager {

    private static Conversation mConversation = null;
    private static DevicesManager mDevicesManager;
    private VideoService mVideoService;
    private static SkypeConversationJoinCallback mSkypeConversationJoinCallback;
    private static SkypeConversationCanLeaveCallback mSkypeConversationCanLeaveCallback;
    private MMVRSurfaceView mParticipantVideoSurfaceView;
    private TextureView mPreviewTextureView;
    Camera frontCamera = null;

    //
    // statics
    //
    private static SkypeManagerImpl sSkypeManager;

    /**
     * Create or return a new instance
     *
     * @param context Your application's context
     * @return a new or existing instance of SkypeManager2
     */
    public static synchronized SkypeManagerImpl getInstance(
            Context context,
            SkypeConversationJoinCallback skypeConversationJoinCallback,
            SkypeConversationCanLeaveCallback skypeConversationCanLeaveCallback) {
        if (null == sSkypeManager) { // initialize a new instance of singleton
            sSkypeManager = new SkypeManagerImpl(
                    Application.getInstance(context)
            );
            mSkypeConversationCanLeaveCallback = skypeConversationCanLeaveCallback;
            mSkypeConversationJoinCallback = skypeConversationJoinCallback;

        }
        return sSkypeManager;
    }

    //
    // Instance fields
    //
    private final Application mSkypeApplication;

    // constructor
    private SkypeManagerImpl(Application skypeApplication) {
        mSkypeApplication = skypeApplication;
    }

    /**
     * Returns a reference to the Skype Application framework
     *
     * @return the Skype application
     */
    public Application getSkypeApplication() {
        return mSkypeApplication;
    }


    @Override
    public void joinConversation(
            URI meetingURI,
            String displayName,
            MMVRSurfaceView videoView,
            TextureView videoPreview) throws SFBException {

        mParticipantVideoSurfaceView = videoView;
        mPreviewTextureView = videoPreview;
        Conversation meeting = null;
        setDisplayName(displayName); // set our name


        try {

            ConversationPropertyChangeListener conversationPropertyChangeListener = new ConversationPropertyChangeListener();
            meeting = getConversation(meetingURI);
            meeting.addOnPropertyChangedCallback(
                    conversationPropertyChangeListener
            );

        } catch (SFBException e) {
            mSkypeConversationJoinCallback
                    .onSkypeConversationJoinFailure(e);
        }



        mDevicesManager = mSkypeApplication.getDevicesManager();
        //get video service, add callback for video,
        //associate surfaces to video service


    }

    @Override
    public void prepareOutgoingVideo() {
        mVideoService = mConversation.getVideoService();
        mVideoService.addOnPropertyChangedCallback(this.onPropertyChangedCallback);

        if (mVideoService.canSetActiveCamera()){
            ArrayList<Camera> cameras = (ArrayList<Camera>) mDevicesManager.getCameras();
            for(Camera camera: cameras) {
                if (camera.getType() == Camera.CameraType.FRONTFACING){
                    try {
                        mVideoService.setActiveCamera(camera);
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        }
        mPreviewTextureView.setSurfaceTextureListener(new VideoPreviewSurfaceTextureListener(this));
    }

    @Override
    public void startOutgoingVideo() {

    }

    @Override
    public void stopOutgoingVideo() {

    }

    VideoService.OnPropertyChangedCallback onPropertyChangedCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            switch(propertyId) {
                case VideoService.CAN_SET_ACTIVE_CAMERA_PROPERTY_ID:
                case VideoService.CAN_SET_PAUSED_PROPERTY_ID:
                    // updateState();
                    break;
                default:
            }
        }
    };
    /**
     * Setup the Video preview.
     * @param texture
     */
    public void SurfaceTextureCreatedCallback(SurfaceTexture texture) {
        try {
            // Display the preview
            mVideoService.displayPreview(texture);

            // Check state of video service.
            // If not started, start it.
            if (mVideoService.canStart()) {
                mVideoService.start();
            } else {
                // On joining the meeting the Video service is started by default.
                // Since the view is created later the video service is paused.
                // Resume the service.
                if (mVideoService.canSetPaused()) {
                    mVideoService.setPaused(false);
                }
            }
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }
    //
    // private methods
    //

    private Conversation getConversation(URI meetingUri) throws SFBException {
        return mSkypeApplication
                .getConversationsManager()
                .getOrCreateConversationMeetingByUri(meetingUri);
    }

    private void setDisplayName(String userDisplayName) {
        // FIXME why do I set my name over here, so that it shows up in a Conversation?
        // strange....
        mSkypeApplication
                .getConfigurationManager()
                .setDisplayName(userDisplayName);
    }
    /**
     * Callback implementation for listening for conversation property changes.
     */
    class ConversationPropertyChangeListener extends Observable.OnPropertyChangedCallback {


        ConversationPropertyChangeListener(){}
         /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            Conversation conversation = (Conversation)sender;
            if (propertyId == Conversation.STATE_PROPERTY_ID) {
                if (conversation.getState() == Conversation.State.ESTABLISHED){
                    //            // pass to callback
                mSkypeConversationJoinCallback
                    .onSkypeConversationJoinSuccess(
                            (Conversation)sender
                    );
                }
            }
            if (propertyId == Conversation.CAN_LEAVE_PROPERTY_ID){
                mSkypeConversationCanLeaveCallback
                        .onSkypeConversationCanLeave(
                                conversation.canLeave());
            }
        }


    }
    // Listener class for TextureSurface
    private class VideoPreviewSurfaceTextureListener implements  TextureView.SurfaceTextureListener {

        private SkypeManagerImpl videoFragment = null;
        public VideoPreviewSurfaceTextureListener(SkypeManagerImpl videoImplementation) {
            videoFragment = videoFragment;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            videoFragment.SurfaceTextureCreatedCallback(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) { }


    };
}
