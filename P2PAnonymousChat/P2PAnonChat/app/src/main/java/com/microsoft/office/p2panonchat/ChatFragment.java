package com.microsoft.office.p2panonchat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.office.p2panonchat.chat.chatContent;
import com.microsoft.office.sfb.appsdk.ChatService;
import com.microsoft.office.sfb.appsdk.SFBException;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.microsoft.office.p2panonchat.R.id.editText;
import static com.microsoft.office.p2panonchat.R.id.sendButton;


/**
 * The chat fragment provides the UI for the chat modality of a
 * conversation. The conversation object is created in the MainActivity
 * and cached as a class field. This ChatFragment is shown when the
 * state of the conversation is connected and the chat modality is
 * available.
 * This fragment has a container for a chat items list fragment which
 * shows the history of the chat. In addition, this fragment has a
 * multi-line EntryText control for the user to compose and send text.
 */
public class ChatFragment extends Fragment
{


    private enum LayoutManagerType {

        GRID_LAYOUT_MANAGER,

        LINEAR_LAYOUT_MANAGER

    }
    private OnFragmentInteractionListener mListener;
    private static ChatService mChatService;
    private ItemFragment mItemFragment;

    @InjectView(editText)
    protected EditText mMessageSendEditText;

    @InjectView(sendButton)
    protected Button mSendButton;

    public ChatFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(ChatService chatService) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        mChatService = chatService;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int scrollPosition = 0;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
//load the item list fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mItemFragment = new ItemFragment();
        transaction.add(R.id.chatList,mItemFragment);
        transaction.commit();

        ButterKnife.inject(this, view);

        mMessageSendEditText.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //Notify remote participant that the user is typing a message
                try {
                    if (mChatService.canSendIsTyping()){
                        mChatService
                                .sendIsTyping();

                    }
                } catch (SFBException e) {
                    e.printStackTrace();
                }
            }
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mChatService
                            .sendMessage(
                                    mMessageSendEditText
                                            .getText()
                                            .toString());
                } catch (SFBException e) {
                    e.printStackTrace();
                }
            }});

        if (mChatService.canSendMessage() == true){
            mMessageSendEditText.setEnabled(true);
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction("button pressed");
        }
    }

    /**
     * When a new message is received, the MainActivity calls updateChatHistory and
     * passes the new message. This method extracts the message content and adds it
     * to a new ChatItem object and passes that object to the list fragment where it is
     * added to the UI.
     * @param newMessage
     */
    public void updateChatHistory(String newMessage){

        //TODO create new chat item, add it list, and give list to ItemFragment
        chatContent.ChatItem chatItem = new chatContent.ChatItem(
                String.valueOf(
                        mItemFragment.ITEMS.size())
                ,newMessage.toString());
        mItemFragment.addChatItem(chatItem);
    }

    /**
     * Called when this fragment is attached to the activity. MainActivity
     * (the listener) is notified when the fragment is attached.
     * @param context
     */
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
        if (mListener != null) {
            mListener.onFragmentInteraction(getActivity().getString(R.string.fragmentCloseAction));
        }
        mListener = null;
    }


    /**
     * A dummy item representing a piece of content.
     */
    public static class ChatItem {
        public final String id;
        public final String content;

        public ChatItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String interaction);
    }

}
