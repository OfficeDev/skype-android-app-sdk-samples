/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 */

package com.microsoft.office.sfb.sfbdemo;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.SFBException;


/**
 * The ChatFragment class shows the list of chat items.
 * It uses a recycler view to show the list of chat items
 *
 * RecyclerView -----------> ChatAdapter     --------> ChatItemPresenter.
 *             (uses)  (RecyclerView.Adapter) (uses)  (RecyclerView.ViewHolder)
 *
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.ChatFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment implements ChatAdapter.ChatAdapterEventListener{

    private String TAG = ChatFragment.class.getSimpleName();
    private ChatAdapter chatAdapter = null;
    private ChatFragmentInteractionListener chatFragmentInteractionListener = null;

    private static Conversation conversation = null;

    public ChatFragment() {
    }

    /**
     * Factory method to create a new instance of this fragment
     *
     * @return A new instance of fragment ChatFragment.
     */
    public static ChatFragment newInstance(Conversation conv) {
        ChatFragment fragment = new ChatFragment();
        conversation = conv;
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ChatFragmentInteractionListener) {
            chatFragmentInteractionListener = (ChatFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = (View)inflater.inflate(R.layout.chat_fragment_layout, container, false);

        Button sendButton = (Button)rootView.findViewById(R.id.sendButtonId);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendButtonClicked(v);
            }
        });
        // Enable the button based on canSendMessage
        sendButton.setEnabled(conversation.getChatService().canSendMessage());

        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(
                R.id.chat_list_recycler);

        this.chatAdapter = new ChatAdapter(this.conversation);
        this.chatAdapter.setAdapterEventListener(this);

        // Set the adapter for recyclerView
        recyclerView.setAdapter(this.chatAdapter);

        // Set the layout manager.
        LinearLayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        chatFragmentInteractionListener = null;
    }

    /**
     * Send the message.
     * @param view
     */
    public void onSendButtonClicked(android.view.View view) {
        InputMethodHelper.hideSoftKeyBoard(view.getContext(), view.getWindowToken());

        EditText messageEditText = (EditText)view.getRootView().findViewById(R.id.messageEditTextId);
        String message = messageEditText.getText().toString();
        if (message != "") {
            try {
                // Send message
                conversation.getChatService().sendMessage(message);

                // Clear the editText view after sending message.
                messageEditText.setText("");
            } catch (SFBException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void someMethod(int position) {
        // Dummy method
    }

    /**
     * Interface to communicate with the activity.
     */
    public interface ChatFragmentInteractionListener {
        void onChatFragmentInteraction();
    }
}
