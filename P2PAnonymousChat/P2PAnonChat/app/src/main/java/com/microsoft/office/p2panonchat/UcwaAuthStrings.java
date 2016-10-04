/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office.p2panonchat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.microsoft.office.p2panonchat.R.id.okButton;
import static com.microsoft.office.p2panonchat.R.id.ucwaURL;
import static com.microsoft.office.p2panonchat.R.id.webToken;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UcwaAuthStrings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UcwaAuthStrings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UcwaAuthStrings extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_UCWA_URL = "param1";
    private static final String ARG_UCWA_TOKEN = "param2";

    // TODO: Rename and change types of parameters
    private String mUCWAURL;
    private String mUCWAToken;

    private OnFragmentInteractionListener mListener;

    @InjectView(ucwaURL)
    protected EditText mUcwaURLEdit;
    @InjectView(webToken)
    protected EditText mUcwaTokenEdit;
    @InjectView(okButton)
    protected Button mOkButton;

    public UcwaAuthStrings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ucwaURL Parameter 1.
     * @param ucwaToken Parameter 2.
     * @return A new instance of fragment UcwaAuthStrings.
     */
    // TODO: Rename and change types and number of parameters
    public static UcwaAuthStrings newInstance(String ucwaURL, String ucwaToken) {
        UcwaAuthStrings fragment = new UcwaAuthStrings();
        Bundle args = new Bundle();
        args.putString(ARG_UCWA_URL, ucwaURL);
        args.putString(ARG_UCWA_TOKEN, ucwaToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUCWAURL = getArguments().getString(ARG_UCWA_URL);
            mUCWAToken = getArguments().getString(ARG_UCWA_TOKEN);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_ucwa_auth_strings, container, false);
        ButterKnife.inject(this, view);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(
                            getActivity().getString(R.string.fragmentInteractionOk),
                            mUCWAURL,
                            mUCWAToken);
                }

            }
        });
        mUcwaURLEdit.setText(mUCWAURL);
        mUcwaTokenEdit.setText(mUCWAToken);
        return view;
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
        mListener.onFragmentInteraction(
                getActivity().getString(
                        R.string.fragmentCloseAction),
                "",
                "");
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
        // TODO: Update argument type and name
        void onFragmentInteraction(String interaction, String ucwaURL, String ucwaToken);
    }
}
