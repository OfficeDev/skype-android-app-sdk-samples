package com.microsoft.office.sfb.sfbwellbaby;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.microsoft.office.sfb.sfbwellbaby.SkypeAPI.SkypeManagerImpl;

import butterknife.InjectView;
import butterknife.OnClick;

import static com.microsoft.office.sfb.sfbwellbaby.R.id.pauseVideoButton;


/**
 * A placeholder fragment containing a simple view.
 */
public class SkypeCallFragment extends Fragment {

    @InjectView(pauseVideoButton)
    public Button mPauseButton;

    private SkypeManagerImpl mSkypeManagerImpl = null;
    private OnFragmentInteractionListener mListener;
    View mRootView;


    public SkypeCallFragment() {
    }

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
        mRootView = inflater.inflate(R.layout.fragment_skype_call, container, false);
        mListener.onFragmentInteraction(mRootView, "inflated");
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

    @OnClick(pauseVideoButton)
    public void onClickVideoPause(Button button) {

        mSkypeManagerImpl.stopOutgoingVideo();
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
        void onFragmentInteraction(View rootView, String newMeetingURI);
    }

}
