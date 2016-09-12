package com.microsoft.office.p2panonchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.microsoft.office.p2panonchat.chat.chatContent;
import com.microsoft.office.p2panonchat.conversationhelper.ConversationHelper;
import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.MessageActivityItem;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.URI;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
        ConversationHelper.ConversationChatCallback {

    UcwaAuthStrings mUcwaAuthStrings;
    ChatFragment mChatFragment;
    Conversation mActiveConversation;
    private static final String SETTINGS_FRAGMENT_STACK_STATE = "UcwaAuthStrings";
    String mUCWAUrl;
    String mUCWAToken;
    Application mApplication;
    private ConversationHelper mConversationHelper;
    protected Boolean mCanSendMessage = false;
    private  String mSaaSToken;


    @Override
    protected void onStop(){
        super.onStop();
        if (mConversationHelper != null) {
            mConversationHelper.removeListeners();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){

        super.onSaveInstanceState(outState);

        outState.putString(
                getString(R.string.userNameStateKey),
                String.valueOf(
                        ((EditText)findViewById(R.id.userName))
                                .getText()));
    }

    /**
     * The bundle only stores primitive state objects. The App SDK
     * Application object and its children are lost when the activity
     * is hidden or the screen is rotated. You cannot restore any
     * active conversation. Instead, start a new conversation with
     * the same remote SIP endpoint.
     * @param savedState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedState){

        ((EditText)findViewById(R.id.userName))
                .setText(
                        savedState.getString(
                                getString(R.string.userNameStateKey)));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
            Snackbar.make(this.getCurrentFocus(), e.getLocalizedMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }
    }

    private void loadChatFragment() {

        hideFloatingButton();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        try {
            mChatFragment = ChatFragment.newInstance(
                    mActiveConversation.getChatService(),
                    mCanSendMessage);


            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack(SETTINGS_FRAGMENT_STACK_STATE);
            fragmentTransaction.commitAllowingStateLoss();

            fragmentTransaction.replace(R.id.fragmentContainer, mChatFragment);
            fragmentTransaction.commit();

        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(this.getCurrentFocus(), e.getLocalizedMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /**
     * Connect to an existing Skype for Business meeting with the URI you get
     * from a server-side UCWA-based web service.
     */
    @SuppressLint("LongLogTag")
    private Conversation startToJoinConversation() {
        URI meetingURI = null;
        Conversation conversation = null;
        try {
            RESTUtility.SaasAPIInterface apiInterface = RESTUtility.getClient();
            String strRequestBody = "body";
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    getString(R.string.getTokenRequestBody));

            Call<SaaSResult> call = apiInterface.getAnonymousToken(requestBody);
            call.enqueue(new Callback<SaaSResult>() {
                @Override
                public void onResponse(Call<SaaSResult> call, Response<SaaSResult> response) {
                   Log.i("get token response", response.body().toString());
                }

                @Override
                public void onFailure(Call<SaaSResult> call, Throwable t) {
                    Log.i("failed token get", t.getLocalizedMessage().toString());
                }
            });
        } catch (UnsupportedOperationException e) {
            Log.e("unsupported operation",
                    e.getLocalizedMessage());
        } catch (Exception ex){
            Log.e("Exception on get SaaS interface",
                    ex.getLocalizedMessage());

        }

        mUCWAUrl = getString(R.string.ucwaURL);
        mUCWAToken = getString(R.string.ucwaTOKEN);
        try {

            //Check for required dangerous permissions before getting the App SDK entry point
            if (appHasPermissions()) {
                String myName = String.valueOf(((EditText)findViewById(R.id.userName)).getText());
                String helpDeskURI = getString(R.string.ucwa_url);
                mApplication = Application.getInstance(getApplicationContext());
                mActiveConversation = mApplication.joinPeerToPeerAnonymously(
                        mUCWAUrl,
                        mUCWAToken,
                        myName,
                        helpDeskURI);

                //Construct a chat-only conversation helper
                mConversationHelper = new ConversationHelper(
                        mActiveConversation,
                        this);

            } else {
                Snackbar.make(this.getCurrentFocus(), "Insufficient permissions", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }

        } catch (SFBException e) {
            e.printStackTrace();
            Snackbar.make(this.getCurrentFocus(), e.getLocalizedMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }catch (RuntimeException se){
            Snackbar.make(this.getCurrentFocus(), se.getLocalizedMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            se.printStackTrace();
        }
        return mActiveConversation;
    }

    /**
     * Check for required dangerous permissions.
     * @return true if the user has granted (and not revoked) required permissions
     */
    private boolean appHasPermissions(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_DENIED){
            return false;
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_DENIED){
            return false;
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED){
            return false;
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            return false;
        }
        return true;
    }

    @Override
    public void onConversationStateChanged(final Conversation.State newConversationState) {
        Log.i("Conv. state CHANGED:", newConversationState.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

                Snackbar.make(fab.getRootView(), newConversationState.toString(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        if (newConversationState == Conversation.State.ESTABLISHED) {
            //Initialize the conversation helper with the established conversation,
            //the SfB App SDK devices manager, the outgoing video TextureView,
            //The view container for the incoming video, and a conversation helper
            //callback.


            loadChatFragment();
        }
    }

    @Override
    public void onCanSendMessage(boolean canSendMessage) {
        mCanSendMessage = canSendMessage;
        if (canSendMessage){
            try {
                mActiveConversation.getChatService().sendMessage("first message");
            } catch (SFBException e) {
                e.printStackTrace();
            }
        }
        if (mChatFragment != null && mChatFragment.isVisible()){
            mChatFragment.setSendButtonEnableState(canSendMessage);
        }
    }

    @Override
    public void onMessageReceived(MessageActivityItem newMessage) {
        mChatFragment.updateChatHistory(newMessage.getText());
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
