package com.microsoft.office.sfb.sfbwellbaby;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.VideoService;

/**
 * A placeholder fragment containing a simple view.
 */
public class SkypeCallFragment extends Fragment {

    private static Conversation mConversation = null;
    private static DevicesManager mDevicesManager;
    private VideoService mVideoService;


    public SkypeCallFragment(){}

    @SuppressLint("ValidFragment")
    public SkypeCallFragment(Conversation mAnonymousMeeting, DevicesManager devicesManager) {
    }
    /**
     * Create the Video fragment.
     *
     * @param conv Conversation
     * @param dManager DevicesManager.
     * @return A new instance of fragment VideoFragment.
     */
    public static SkypeCallFragment newInstance(Conversation conv, DevicesManager dManager) {
        SkypeCallFragment fragment = new SkypeCallFragment(conv, dManager);
        mConversation = conv;
        mDevicesManager = dManager;
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_skype_call, container, false);

        mVideoService = mConversation.getVideoService();
        mVideoService.addOnPropertyChangedCallback(this.onPropertyChangedCallback);
        return rootView;
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
}
