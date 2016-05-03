package com.microsoft.office.sfb.healthcare;

import java.net.URI;
import java.util.List;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.TextureView;

import com.microsoft.media.MMVRSurfaceView;
import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.AudioService;
import com.microsoft.office.sfb.appsdk.ChatService;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.ConversationActivityItem;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.HistoryService;
import com.microsoft.office.sfb.appsdk.MessageActivityItem;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.ObservableList;
import com.microsoft.office.sfb.appsdk.Participant;
import com.microsoft.office.sfb.appsdk.ParticipantAudio;
import com.microsoft.office.sfb.appsdk.ParticipantService;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.appsdk.VideoService;
import com.microsoft.office.sfb.appsdk.Camera;

/**
 * This is a convenience class.  It simplifies interaction with the core Conversation interface
 * and its children.
 *
 * It provides the following functionality:
 * 1. An integrated callback interface for the most useful property change notifications,
 *    removing the need to write verbose observer code.
 * 2. Audio functionality to toggle mute and switch between loudspeaker and non-loudspeaker
 *    endpoints.
 * 3. Video functionality to start outgoing and incoming video and switch between cameras. 
 */
public class ConversationHelper {

    /**
     * Callback interface for property and state change notifications.
     */
    public interface ConversationCallback {
        /**
         * This method is called when the state of the conversation changes.
         * E.g. On joining a meeting the conversation state changes from idle->establishing->established.
         * @param newConversationState The new conversation state.
         */
        void onConversationStateChanged(Conversation.State newConversationState);

        /**
         * This method is called when the {@link ChatService#CAN_SEND_MESSAGE_PROPERTY_ID} changes.
         * @param canSendMessage New value retrieved using {@link ChatService#canSendMessage()}
         */
        void onCanSendMessage(boolean canSendMessage);

        /**
         * This method is called when a new incoming IM ({@link MessageActivityItem}) is received.
         * @param newMessage Incoming MessageActivityItem retrieved by listening to changes on the
         *                   {@link HistoryService#getConversationActivityItems() Activity Item Collection}
         */
        void onMessageReceived(MessageActivityItem newMessage);

        /**
         * This method is called when the state of the self participant Audio state changes.
         * @param newState The new state of Audio {@link com.microsoft.office.sfb.appsdk.ParticipantService.State}
         *                 retrieved by calling {@link ParticipantAudio#getState()}
         */
        void onSelfAudioStateChanged(ParticipantService.State newState);

        /**
         * This method is called when the mute status of local participant changes.
         * {@link ParticipantAudio#PARTICIPANT_SERVICE_STATE_PROPERTY_ID}
         * @param newMuteStatus The new mute status retrieved calling {@link ParticipantAudio#isMuted()}
         */
        void onSelfAudioMuteChanged(boolean newMuteStatus);

        /**
         * This method is called when the state of {@link com.microsoft.office.sfb.appsdk.ConversationService#CAN_START_PROPERTY_ID}
         * changes.
         * @param newCanStart The new value retrieved by calling {@link VideoService#canStart()}
         */
        void onCanStartVideoServiceChanged(boolean newCanStart);

        /**
         * This method is called when the state of {@link VideoService#CAN_SET_PAUSED_PROPERTY_ID}
         * changes.
         * @param newSetCanPaused The new value retrieved by calling {@link VideoService#canSetPaused()}
         */
        void onCanSetPausedVideoServiceChanged(boolean newCanSetPaused);


        /**
         * This method is called when the state of {@link VideoService#CAN_SET_ACTIVE_CAMERA_PROPERTY_ID} changes.
         * changes.
         * @param newCanSetActiveCamera The new value retrieved by calling {@link VideoService#canSetActiveCamera()}
         */
        void onCanSetActiveCameraChanged(boolean newCanSetActiveCamera);
    }

    private Conversation conversation = null;
    private DevicesManager devicesManager = null;
    private AudioService audioService = null;
    private VideoService videoService = null;
    private ChatService chatService = null;
    private HistoryService historyService = null;

    /**
     * Self participant
     */
    private Participant selfParticipant = null;
    private ParticipantAudio selfParticipantAudio = null;

    /**
     * Self participant video preview control.
     */
    private TextureView videoPreviewView = null;

    /**
     * Remote participant video control.
     */
    private MMVRSurfaceView participantVideoView = null;

    /**
     * Callback passed in by the caller.
     */
    private ConversationCallback conversationCallback = null;

    private ObservableList.OnListChangedCallback listChangedCallback = null;

    /**
     * Callback handler class. This handles the property change notifications from SDK entities.
     * It extends {@link Observable.OnPropertyChangedCallback}
     */
    private ConversationCallbackHandler conversationCallbackHandler = null;

    /**
     * Constructor.
     * @param conversation Conversation created by calling {@link Application#joinMeetingAnonymously(String, URI)}
     * @param devicesManager DevicesManager instance {@link Application#getDevicesManager()}
     * @param textureView Self video preview view.
     * @param mmvrSurfaceView Remote participant video view.
     * @param conversationCallback {@link ConversationCallback} object that should receive 
     *        callbacks from this conversation helper.
     */
    public ConversationHelper(Conversation conversation,
                              DevicesManager devicesManager,
                              TextureView textureView,
                              MMVRSurfaceView mmvrSurfaceView,
                              ConversationCallback conversationCallback) {


        // Setup the callback and callback handler.
        this.conversationCallback = conversationCallback;
        this.conversationCallbackHandler = new ConversationCallbackHandler();
        this.listChangedCallback = new MessageListCallbackHandler();

        this.conversation = conversation;
        this.conversation.addOnPropertyChangedCallback(this.conversationCallbackHandler);
        this.devicesManager = devicesManager;

        // Get the chat service and register for property change notifications.
        this.chatService = conversation.getChatService();
        this.chatService.addOnPropertyChangedCallback(this.conversationCallbackHandler);

        // Get the audio service and register for property change notifications.
        this.audioService = conversation.getAudioService();
        this.audioService.addOnPropertyChangedCallback(this.conversationCallbackHandler);

        // Get the video service and register for property change notifications.
        this.videoService = conversation.getVideoService();
        this.videoService.addOnPropertyChangedCallback(this.conversationCallbackHandler);

        this.historyService = conversation.getHistoryService();
        this.historyService.getConversationActivityItems().addOnListChangedCallback(this.listChangedCallback);

        this.selfParticipant = conversation.getSelfParticipant();
        this.selfParticipantAudio = this.selfParticipant.getParticipantAudio();
        this.selfParticipantAudio.addOnPropertyChangedCallback(this.conversationCallbackHandler);

        this.videoPreviewView = textureView;
        this.videoPreviewView.setSurfaceTextureListener(new VideoPreviewSurfaceTextureListener());
        this.participantVideoView = mmvrSurfaceView;
        this.participantVideoView.setCallback(new VideoStreamSurfaceListener());

    }

    /**
     * Switch between Loudspeaker and Non-loudspeaker.
     */
    public void changeSpeakerEndpoint() {
        DevicesManager.Endpoint endpoint = DevicesManager.Endpoint.LOUDSPEAKER;
        switch(this.devicesManager.getActiveEndpoint()) {
            case LOUDSPEAKER:
                endpoint = DevicesManager.Endpoint.NONLOUDSPEAKER;
                break;
            case NONLOUDSPEAKER:
                endpoint = DevicesManager.Endpoint.LOUDSPEAKER;
                break;
        }
        this.devicesManager.setActiveEndpoint(endpoint);
    }

    /**
     * Toggle mute state.
     */
    public void toggleMute() {
        // Get current mute state.
        boolean selfMuteState = this.selfParticipantAudio.isMuted();
        try {
            this.selfParticipantAudio.setMuted(!selfMuteState);
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pause or un-pause video.
     */
    public void toggleVideoPaused() {
        boolean videoPaused = this.videoService.getPaused();
        try {
            this.videoService.setPaused(!videoPaused);
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch the camera by selecting from the list of available cameras.
     */
    public void changeActiveCamera() {
        try {
            Camera activeCamera = this.videoService.getActiveCamera();
            List<Camera> availableCameras = devicesManager.getCameras();
            int newCameraIndex = (availableCameras.indexOf(activeCamera) + 1) % availableCameras.size();
            this.videoService.setActiveCamera(availableCameras.get(newCameraIndex));
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start outgoing video.
     */
    public void startOutgoingVideo() {
        if (this.videoPreviewView.isAvailable()) {
            surfaceTextureCreatedCallback(this.videoPreviewView.getSurfaceTexture());
        }
    }

    /**
     * Start incoming video.
     */
    public void startIncomingVideo() {
        if (this.participantVideoView.isActivated()) {
            videoStreamSurfaceCreatedCallback(this.participantVideoView);
        }
    }

    /**
     * Helper method to ensure that the video service is started and video is flowing.
     */
    public void ensureVideoIsStartedAndRunning() {
        try {
            // Check state of video service.
            // If not started, start it.
            if (this.videoService.canStart()) {
                this.videoService.start();
            } else {
                // On joining the meeting the Video service is started by default if we have video
                // Since the view is created later the video service is paused.
                // Resume the service.
                if (this.videoService.getPaused()) {
                    if (this.videoService.canSetPaused()) {
                        this.videoService.setPaused(false);
                    }
                }
            }
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    /**
     * For displaying the video preview, we need to tie the video stream to the TextureView control.
     * This is achieved by registering a listener to the TextureView by passing in an instance of
     * class below.
     * Clients are expected to pass in the view once inflated from their activity / fragments.
     */
    private class VideoPreviewSurfaceTextureListener implements  TextureView.SurfaceTextureListener {

        /**
         * This method is called when the view is available. We immediately register is with the
         * {@link VideoService#displayPreview(SurfaceTexture)} in the callback handler.
         * @param surface
         * @param width
         * @param height
         */
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            surfaceTextureCreatedCallback(surface);
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

    /**
     * For displaying the remote participant video we need to tie the video stream to the MMVRSurfaceView
     * control. This is achieved by registering a listener to the MMVRSurfaceView by passing in an instance of
     * class below.
     * Clients are expected to pass in the view once inflated from their activity / fragments.
     */
    private class VideoStreamSurfaceListener implements MMVRSurfaceView.MMVRCallback {

        /**
         * This method is called when the MMVRSurfaceView is created. We will tie in the video stream
         * to the control by calling {@link VideoService#displayParticipantVideo(MMVRSurfaceView)}
         * @param mmvrSurfaceView
         */
        @Override
        public void onSurfaceCreated(MMVRSurfaceView mmvrSurfaceView) {
            videoStreamSurfaceCreatedCallback(mmvrSurfaceView);
        }

        @Override
        public void onFrameRendered(MMVRSurfaceView mmvrSurfaceView) {}

        @Override
        public void onRenderSizeChanged(MMVRSurfaceView mmvrSurfaceView, int i, int i1) {}

        @Override
        public void onSmartCropInfoChanged(MMVRSurfaceView mmvrSurfaceView, int i, int i1, int i2,
                                           int i3, int i4) {}
    }

    /**
     * Setup the Video preview.
     * @param texture SurfaceTexture
     */
    private void surfaceTextureCreatedCallback(SurfaceTexture texture) {
        try {
            // Tie the video stream to the texture view control
            videoService.displayPreview(texture);

            // Check state of video service.
            // If not started, start it.
            if (this.videoService.canStart()) {
                this.videoService.start();
            } else {
                // On joining the meeting the Video service is started by default.
                // Since the view is created later the video service is paused.
                // Resume the service.
                if (this.videoService.canSetPaused()) {
                    this.videoService.setPaused(false);
                }
            }
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup the remote participant video.
     * @param mmvrSurfaceView MMVRSurfaceView
     */
    private void videoStreamSurfaceCreatedCallback(MMVRSurfaceView mmvrSurfaceView) {
        this.participantVideoView = mmvrSurfaceView;
        // Setup the video properties
        this.participantVideoView.setAutoFitMode(MMVRSurfaceView.MMVRAutoFitMode_Crop);
        this.participantVideoView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        // Render the video
        this.participantVideoView.requestRender();
        try {
            // Tie the video stream to the MMVRSurfaceView control.
            this.videoService.displayParticipantVideo(this.participantVideoView);
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    /**
     * This callback handler class handles property change notifications from SDK entities.
     * We have authored a single handler class that distinguishes who the sender was.
     */
    class ConversationCallbackHandler extends Observable.OnPropertyChangedCallback {
        /**
         * onProperty changed will be called by the Observable instance on a property change.
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         * @see com.microsoft.office.sfb.appsdk.Observable.OnPropertyChangedCallback
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {

            if (Conversation.class.isInstance(sender)) {
                Conversation conversation = (Conversation)sender;
                switch(propertyId) {
                    case Conversation.STATE_PROPERTY_ID:
                        Conversation.State newState = conversation.getState();
                        conversationCallback.onConversationStateChanged(newState);
                        break;
                }
            }

            if (ChatService.class.isInstance(sender)) {
                ChatService chatService = (ChatService)sender;
                switch (propertyId) {
                    case ChatService.CAN_SEND_MESSAGE_PROPERTY_ID:
                        boolean canSendMessage = chatService.canSendMessage();
                        conversationCallback.onCanSendMessage(canSendMessage);
                        break;
                }
            }

            if (ParticipantAudio.class.isInstance(sender)) {
                ParticipantAudio selfParticipantAudio = (ParticipantAudio)sender;
                switch (propertyId) {
                    case ParticipantService.PARTICIPANT_SERVICE_STATE_PROPERTY_ID:
                        conversationCallback.onSelfAudioStateChanged(selfParticipantAudio.getState());
                        break;
                    case ParticipantAudio.PARTICIPANT_IS_MUTED_PROPERTY_ID:
                        conversationCallback.onSelfAudioMuteChanged(selfParticipantAudio.isMuted());
                        break;
                }
            }

            if (VideoService.class.isInstance(sender)) {
                VideoService videoService = (VideoService)sender;
                switch (propertyId) {
                    case VideoService.CAN_START_PROPERTY_ID:
                        conversationCallback.onCanStartVideoServiceChanged(videoService.canStart());
                        break;
                    case VideoService.CAN_SET_PAUSED_PROPERTY_ID:
                        conversationCallback.onCanSetPausedVideoServiceChanged(videoService.canSetPaused());
                        break;
                    case VideoService.CAN_SET_ACTIVE_CAMERA_PROPERTY_ID:
                        conversationCallback.onCanSetActiveCameraChanged(videoService.canSetActiveCamera());
                        break;
                }
            }
        }
    }

    /**
     * This callback handler class handles change notifications on {@link ObservableList}
     */
    class MessageListCallbackHandler extends ObservableList.OnListChangedCallback {
        @Override
        public void onChanged(Object sender) { }

        @Override
        public void onItemRangeChanged(Object sender, int positionStart, int itemCount) {}

        /**
         * Called whenever items have been inserted into the list.
         *
         * @param sender        ObservableList instance
         * @param positionStart starting index of the inserted items.
         * @param itemCount     number of items that have changed
         */
        @Override
        public void onItemRangeInserted(Object sender, int positionStart, int itemCount) {
            ObservableList<?> messageList = (ObservableList<ConversationActivityItem>) sender;

            // Is this a message activity item?
            if (MessageActivityItem.class.isInstance(messageList.get(positionStart))) {
                MessageActivityItem messageActivityItem = (MessageActivityItem)messageList.get(positionStart);

                // Is this an incoming message?
                if (messageActivityItem.getDirection() == MessageActivityItem.MessageDirection.INCOMING) {
                    conversationCallback.onMessageReceived(messageActivityItem);
                }
            }
        }

        @Override
        public void onItemRangeMoved(Object sender, int fromPosition, int toPosition, int itemCount) { }

        @Override
        public void onItemRangeRemoved(Object sender, int positionStart, int itemCount) { }
    }

}
