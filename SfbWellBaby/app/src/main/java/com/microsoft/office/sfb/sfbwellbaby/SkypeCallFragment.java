package com.microsoft.office.sfb.sfbwellbaby;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManagerImpl;

import java.net.URI;
import java.net.URISyntaxException;

import butterknife.InjectView;
import butterknife.OnClick;

import static com.microsoft.office.sfb.sfbwellbaby.R.id.pauseVideoButton;


/**
 * A placeholder fragment containing a simple view.
 */
public class SkypeCallFragment extends Fragment {

    @InjectView(pauseVideoButton)
    Button mPauseButton;

    private SkypeManagerImpl mSkypeManagerImpl = null;

    public SkypeCallFragment(){}

    @SuppressLint("ValidFragment")
    public SkypeCallFragment(SkypeManagerImpl skypeManager) {
        mSkypeManagerImpl = skypeManager;
    }
    /**
     * Create the Video fragment.
     *

     * @return A new instance of fragment VideoFragment.
     */
    public static SkypeCallFragment newInstance(SkypeManagerImpl skypeManager) {
        SkypeCallFragment fragment = new SkypeCallFragment(skypeManager);

        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_skype_call, container, false);

        SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.meetingURIKey), 0);
        String meetingURIString = settings.getString(getString(R.string.meetingURIKey), "");
        URI meetingURI = null;
        try {
            meetingURI = new URI(meetingURIString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            mSkypeManagerImpl.joinConversation(
                    meetingURI,
                    getActivity().getString(R.string.fatherName),

                    (TextureView) rootView.findViewById(R.id.selfParticipantVideoView));
        } catch (SFBException e) {
            e.printStackTrace();
        }
        return rootView;
    }

    @OnClick(pauseVideoButton)
    public void onClickVideoPause(View button){

        mSkypeManagerImpl.stopOutgoingVideo();
    }
}
