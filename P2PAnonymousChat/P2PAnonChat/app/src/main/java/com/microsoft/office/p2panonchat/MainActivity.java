package com.microsoft.office.p2panonchat;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.microsoft.office.p2panonchat.chat.chatContent;
import com.microsoft.office.p2panonchat.conversationhelper.ConversationHelper;
import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.MessageActivityItem;
import com.microsoft.office.sfb.appsdk.ParticipantService;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.URI;


/**
 * the MainActivity class is responsible for starting a new conversation
 * with the remote URI supplied by a user in the MainActivityFragment.
 * The UCWA REST endpoint and web token that are required to connect
 * to a new conversation are stored in Strings.xml. In a production
 * app, these values would be obtained from a SaaS app based on UCAP.
 */

public class MainActivity extends AppCompatActivity implements
        UcwaAuthStrings.OnFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener,
        ItemFragment.OnListFragmentInteractionListener,
        ConversationHelper.ConversationCallback {

    UcwaAuthStrings mUcwaAuthStrings;
    ChatFragment mChatFragment;
    Conversation mActiveConversation;
    private static final String SETTINGS_FRAGMENT_STACK_STATE = "UcwaAuthStrings";
    String mUCWAUrl;
    String mUCWAToken;
    Application mApplication;
    private ConversationHelper mConversationHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState != null) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        MainActivityFragment mainFragment = new MainActivityFragment();
        transaction.add(R.id.fragmentContainer, mainFragment);
        transaction.commit();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.startConversationSnackText, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (startToJoinConversation() == null) {
                    Log.e("Conversation get failed", "MainActivity");
                }
                //work around to test chat fragment
                loadChatFragment();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            loadSettingsFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void hideFloatingButton() {
        //hide the floating button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
    }

    private void showFloatingButton() {
        //show the floating button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
    }

    private void loadSettingsFragment() {

        hideFloatingButton();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        try {
            mUcwaAuthStrings = UcwaAuthStrings.newInstance(
                    getString(R.string.ucwaURL),
                    getString(R.string.ucwaTOKEN));

            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(SETTINGS_FRAGMENT_STACK_STATE);
            fragmentTransaction.commitAllowingStateLoss();

            fragmentTransaction.replace(R.id.fragmentContainer, mUcwaAuthStrings);
            fragmentTransaction.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadChatFragment() {

        hideFloatingButton();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        try {
            mChatFragment = ChatFragment.newInstance(
                    mActiveConversation.getChatService());


            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(SETTINGS_FRAGMENT_STACK_STATE);
            fragmentTransaction.commitAllowingStateLoss();

            fragmentTransaction.replace(R.id.fragmentContainer, mChatFragment);
            fragmentTransaction.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect to an existing Skype for Business meeting with the URI you get
     * from a server-side UCWA-based web service.
     */
    private Conversation startToJoinConversation() {
        URI meetingURI = null;
        Conversation conversation = null;

        mUCWAUrl = getString(R.string.ucwaURL);
        mUCWAToken = getString(R.string.ucwaTOKEN);
        try {

            mApplication = Application.getInstance(this);
            mActiveConversation = mApplication.joinPeerToPeerAnonymously(
                    mUCWAUrl,
                    mUCWAToken,
                    getString(R.string.your_name),
                    getString(R.string.helpdesk_uri));

        } catch (SFBException e) {
            e.printStackTrace();
        }
        return mActiveConversation;
    }

    @Override
    public void onConversationStateChanged(Conversation.State newConversationState) {
        Log.i("Conversation:", newConversationState.toString());

        if (newConversationState == Conversation.State.ESTABLISHED) {
            //Initialize the conversation helper with the established conversation,
            //the SfB App SDK devices manager, the outgoing video TextureView,
            //The view container for the incoming video, and a conversation helper
            //callback.
            mConversationHelper = new ConversationHelper(
                    mActiveConversation,
                    mApplication.getDevicesManager(),
                    null,
                    null,
                    this);

            loadChatFragment();
        }
    }

    @Override
    public void onCanSendMessage(boolean canSendMessage) {

    }

    @Override
    public void onMessageReceived(MessageActivityItem newMessage) {
        mChatFragment.updateChatHistory(newMessage.getText());
    }

    @Override
    public void onSelfAudioStateChanged(ParticipantService.State newState) {

    }

    @Override
    public void onSelfAudioMuteChanged(boolean newMuteStatus) {

    }

    @Override
    public void onCanStartVideoServiceChanged(boolean newCanStart) {

    }

    @Override
    public void onCanSetPausedVideoServiceChanged(boolean newCanSetPaused) {

    }

    @Override
    public void onCanSetActiveCameraChanged(boolean newCanSetActiveCamera) {

    }


    /**
     * ChatFragment fragment interactionlistener
     */
    @Override
    public void onFragmentInteraction(String interaction) {

        if (interaction.contains(getString(R.string.fragmentCloseAction))) {
            showFloatingButton();
            if (mActiveConversation.canLeave()) {
                try {
                    mActiveConversation.leave();
                } catch (SFBException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Interaction with ItemFragment.java
     *
     * @param item
     */
    @Override
    public void onListFragmentInteraction(chatContent.ChatItem item) {
        //Item fragment is loaded

    }

    /**
     * Settings fragment interaction
     *
     * @param ucwaURL
     * @param ucwaToken
     */
    @Override
    public void onFragmentInteraction(String interaction, String ucwaURL, String ucwaToken) {
        mUCWAUrl = ucwaURL;
        mUCWAToken = ucwaToken;
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction().remove(mUcwaAuthStrings).commit();
        showFloatingButton();

    }
}
