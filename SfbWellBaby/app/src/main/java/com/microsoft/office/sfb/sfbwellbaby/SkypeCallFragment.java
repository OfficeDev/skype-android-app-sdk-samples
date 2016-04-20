package com.microsoft.office.sfb.sfbwellbaby;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManager;


/**
 * A placeholder fragment containing a simple view.
 */
public class SkypeCallFragment extends Fragment implements SkypeManager.SkypeVideoReady {

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
        mListener.onFragmentInteraction(mRootView, getActivity().getString(R.string.callFragmentInflated));
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        } catch (Exception e) {
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
        if (mListener != null) {
            mListener.onFragmentInteraction(
                    mRootView,
                    getActivity().
                            getString(R.string.leaveCall));
            mListener = null;
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    public void onDetach() {
        super.onDetach();
        //mCalled = true;
    }

}
