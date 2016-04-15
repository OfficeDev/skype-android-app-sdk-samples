package com.microsoft.office.sfb.sfbwellbaby;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManager;


/**
 * A placeholder fragment containing a simple view.
 */
public class SkypeCallFragment extends Fragment implements SkypeManager.SkypeVideoReady{

    //    @InjectView(pauseVideoButton)
    public Button mPauseButton;
    public Button mEndCallButton;
    private OnFragmentInteractionListener mListener;
    View mRootView;

    @SuppressLint("ValidFragment")
    public SkypeCallFragment() {
    }

    /**
     * Create the Video fragment.
     *
     * @return A new instance of fragment VideoFragment.
     */
    public static SkypeCallFragment newInstance() {
        SkypeCallFragment fragment = new SkypeCallFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_skype_call, container, false);
        mListener.onFragmentInteraction(mRootView, getActivity().getString(R.string.callFragmentInflated));
        mPauseButton = (Button) mRootView.findViewById(R.id.pauseVideoButton);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentInteraction(
                        mRootView,
                        getActivity().
                                getString(R.string.pauseCall));
            }
        });
        mEndCallButton = (Button) mRootView.findViewById(R.id.endCallButton);
        mEndCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentInteraction(
                        mRootView,
                        getActivity().
                                getString(R.string.leaveCall));
            }
        });
        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onSkypeIncomingVideoReady() {

    }

    @Override
    public void onSkypeOutgoingVideoReady(final boolean paused) {
        try {
            this.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (paused == true)
                        mPauseButton.setText("pause");
                    else
                        mPauseButton.setText("resume");
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Used to interact with parent activity
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View rootView, String newMeetingURI);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().finish();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.muteAudioMenuItem:
                mListener.onFragmentInteraction(
                        mRootView,
                        getActivity()
                                .getString(R.string.muteAudio));
                break;
            case R.id.pauseVideoMenuItem:
                mListener.onFragmentInteraction(
                        mRootView,
                        getActivity()
                                .getString(R.string.pauseCall));
                break;
        }

        return false;
    }
}
