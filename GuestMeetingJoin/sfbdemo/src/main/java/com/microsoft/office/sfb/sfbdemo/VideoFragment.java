package com.microsoft.office.sfb.sfbdemo;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.microsoft.media.MMVRSurfaceView;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.Participant;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.appsdk.VideoService;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.Camera;

import java.util.ArrayList;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * The video fragment shows the local participant video preview and
 * the incoming video from the default video channel.
 *
 * Local participant Video Preview:
 * To show video preview the TextureView is used.
 * This sample demonstrates attaching to the TextureView, by passing in the SurfaceTexture
 * obtained from the TextureView to the VideoService::display API.
 *
 * Incoming Video:
 * To display the incoming video, MMVRSurfaceView is provided.
 * The MMVRSurfaceView is an implementation of GLSurfaceView which provides custom rendering.
 * The sample demonstrates attaching to the MMVRSurfaceView passing it to the
 * VideoService::displayParticipantVideo API.
 * Note:
 * This is a temporary API till the implementation to display remote ParticipantVideo
 * is provided.
 */
public class VideoFragment extends Fragment{

    private OnFragmentInteractionListener mListener;

    private static Conversation conversation = null;
    private static DevicesManager devicesManager = null;

    private VideoService videoService = null;

    private TextureView videoPreviewTextureView = null;

    ArrayList<Camera> cameras = null;

    boolean videoPaused = false;

    Camera.Type currentCameraType = Camera.Type.FRONTFACING;

    Camera frontCamera = null;
    Camera backCamera = null;

    Participant dominantSpeaker = null;

    MMVRSurfaceView mmvrSurfaceView = null;
    MMVRSurfaceView mmvrSurface = null;
    RelativeLayout participantVideoLayout = null;

    Button pauseButton = null;
    Button cameraButton = null;

    public VideoFragment() {}

    /**
     * Create the Video fragment.
     *
     * @param conv Conversation
     * @param dManager DevicesManager.
     * @return A new instance of fragment VideoFragment.
     */
    public static VideoFragment newInstance(Conversation conv, DevicesManager dManager) {
        VideoFragment fragment = new VideoFragment();
        conversation = conv;
        devicesManager = dManager;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.cameras = (ArrayList<Camera>) devicesManager.getCameras();
        for(Camera camera: this.cameras) {
            if (camera.getType() == Camera.Type.FRONTFACING)
                this.frontCamera = camera;
            if (camera.getType() == Camera.Type.BACKFACING)
                this.backCamera = camera;
        }
    }

    // Listener class for TextureSurface
    private class VideoPreviewSurfaceTextureListener implements  TextureView.SurfaceTextureListener {

        private VideoFragment videoFragment = null;
        public VideoPreviewSurfaceTextureListener(VideoFragment videoFragment) {
            this.videoFragment = videoFragment;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            this.videoFragment.SurfaceTextureCreatedCallback(surface);
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

    // Listener class for MMVRSurfaceView.
    private class VideoStreamSurfaceListener implements MMVRSurfaceView.MMVRCallback {

        private VideoFragment videoFragment = null;
        public VideoStreamSurfaceListener(VideoFragment videoFragment) {
            this.videoFragment = videoFragment;
        }

        @Override
        public void onSurfaceCreated(MMVRSurfaceView mmvrSurfaceView) {
            VideoStreamSurfaceCreatedCallback(mmvrSurfaceView);
        }

        @Override
        public void onFrameRendered(MMVRSurfaceView mmvrSurfaceView) {}

        @Override
        public void onRenderSizeChanged(MMVRSurfaceView mmvrSurfaceView, int i, int i1) {}

        @Override
        public void onSmartCropInfoChanged(MMVRSurfaceView mmvrSurfaceView, int i, int i1, int i2,
                                           int i3, int i4) {}
    }

    VideoService.OnPropertyChangedCallback onPropertyChangedCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            switch(propertyId) {
                case VideoService.CAN_SET_ACTIVE_CAMERA_PROPERTY_ID:
                case VideoService.CAN_SET_PAUSED_PROPERTY_ID:
                    updateState();
                    break;
                default:
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.video_fragment_layout, container, false);

        // Get the video service and subscribe to property change notifications.
        this.videoService = conversation.getVideoService();
        this.videoService.addOnPropertyChangedCallback(this.onPropertyChangedCallback);

        // Setup the Video Preview
        // Note:
        // The only reason we have created a VideoPreviewSurfaceTextureListener is so that we can
        // immediately bind to it when the view is created. No requirement to do so.
        this.videoPreviewTextureView = (TextureView)rootView.findViewById(R.id.selfParticipantVideoView);
        this.videoPreviewTextureView.setSurfaceTextureListener(new VideoPreviewSurfaceTextureListener(this));


        this.pauseButton = (Button)rootView.findViewById(R.id.pauseVideoButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseClick(v);
            }
        });

        this.cameraButton = (Button)rootView.findViewById(R.id.switchCameraButtonId);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCameraSwitch(v);
            }
        });

        // Setup the Incoming Video View
        // Note:
        // The only reason we have created a VideoStreamSurfaceListener is so that we can
        // immediately bind to it when the view is created. No requirement to do so.
        this.participantVideoLayout = (RelativeLayout)rootView.findViewById(R.id.participantVideoLayoutId);
        this.mmvrSurface = (MMVRSurfaceView)rootView.findViewById(R.id.mmvrSurfaceViewId);
        this.mmvrSurface.setCallback(new VideoStreamSurfaceListener(this));

        // Sample e.g. below to dynamically create the MMVRView.
        //this.mmvrSurface = new MMVRSurfaceView(this.participantVideoLayout.getContext());
        // Add view to layout.
        // this.participantVideoLayout.addView(this.mmvrSurface);

        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Setup the Video preview.
     * @param texture
     */
    public void SurfaceTextureCreatedCallback(SurfaceTexture texture) {
        try {
            // Display the preview
            videoService.showPreview(texture);

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
     * Setup the default incoming video channel preview.
     * @param mmvrSurfaceView
     */
    public void VideoStreamSurfaceCreatedCallback(MMVRSurfaceView mmvrSurfaceView) {
        this.mmvrSurfaceView = mmvrSurfaceView;
        this.mmvrSurfaceView.setAutoFitMode(MMVRSurfaceView.MMVRAutoFitMode_Crop);
        this.mmvrSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        this.mmvrSurfaceView.requestRender();
        try {
            this.videoService.displayParticipantVideo(this.mmvrSurfaceView);
        } catch (SFBException e) {
            e.printStackTrace();
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateState();
            }
        });

    }

    /**
     * Pause Button click handler
     * @param view
     */
    public void onPauseClick(android.view.View view) {
        this.videoPaused = this.videoService.getPaused();
        try {
            this.videoService.setPaused(!this.videoPaused);
            this.updateState();
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Camera Switch click button handler.
     * @param view
     */
    public void onCameraSwitch(android.view.View view) {
        Camera camera = null;
        Camera.Type cameraType = null;
        switch(currentCameraType){
            case FRONTFACING:
                camera = this.backCamera;
                cameraType = Camera.Type.BACKFACING;
                break;
            case BACKFACING:
                camera = this.frontCamera;
                cameraType = Camera.Type.FRONTFACING;
                break;
            default:
        }
        try {
            this.videoService.setActiveCamera(camera);
            this.currentCameraType = cameraType;
            this.updateState();
        }
        catch (SFBException e) {
            e.printStackTrace();
        }

    }

    /**
     * Update UI state.
     */
    private void updateState() {
        this.cameraButton.setEnabled(this.videoService.canSetActiveCamera());

        this.videoPaused = this.videoService.getPaused();
        this.pauseButton.setEnabled(this.videoService.canSetPaused());
        String text = this.videoPaused ? "Resume" : "Pause";
        this.pauseButton.setText(text);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            //throw new RuntimeException(context.toString()
            //        + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
