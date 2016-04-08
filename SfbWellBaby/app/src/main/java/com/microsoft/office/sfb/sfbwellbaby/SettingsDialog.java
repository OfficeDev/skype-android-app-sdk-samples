package com.microsoft.office.sfb.sfbwellbaby;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsDialog extends DialogFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private String mMeetingURI;
    private EditText mEditURI;
    private OnFragmentInteractionListener mListener;

    public SettingsDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment SettingsDialog.
     */
    public static SettingsDialog newInstance(String param1) {
        SettingsDialog fragment = new SettingsDialog();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMeetingURI = getArguments().getString(ARG_PARAM1);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_settings_dialog, container, false);

        ButterKnife.inject(getActivity());
        EditText meetingURIEditor = (EditText) rootView.findViewById(R.id.meetingURIText);
        SharedPreferences settings = getActivity().getSharedPreferences(getActivity().getString(R.string.meetingURIKey), 0);
        String meetingURI = settings.getString(getActivity().getString(R.string.meetingURIKey), "");
        if (meetingURI.isEmpty())
            meetingURIEditor.setText(mMeetingURI);
        else
            meetingURIEditor.setText(meetingURI);

        // Watch for button clicks.
        Button button = (Button) rootView.findViewById(R.id.closeButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // We need an Editor object to make preference changes.
                // All objects are from android.context.Context
                String preferenceKey = getActivity().getString(R.string.meetingURIKey);
                SharedPreferences settings = getActivity().getSharedPreferences(preferenceKey, 0);
                SharedPreferences.Editor editor = settings.edit();
                String newString = ((EditText) rootView.findViewById(R.id.meetingURIText)).getText().toString();
                editor.putString(preferenceKey, newString);

                // Commit the edits!
                editor.commit();
                if (mListener != null) {
                    mListener.onFragmentInteraction(newString);
                }
            }
        });

        return rootView;
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
        void onFragmentInteraction(String newMeetingURI);
    }
}
