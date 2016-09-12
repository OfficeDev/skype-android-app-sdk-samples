package com.microsoft.office.p2panonchat;

import android.annotation.SuppressLint;
import android.util.Log;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;



public class RESTUtility {

    private static SaasAPIInterface saaSAPIInterface;
    private static String baseUrl = "https://metiobank.cloudapp.net/GetAnonTokenJob/" ;

    @SuppressLint("LongLogTag")
    public static SaasAPIInterface getClient() {
        if (saaSAPIInterface == null) {

            try {
                okhttp3.OkHttpClient okClient = new okhttp3.OkHttpClient();

                Retrofit client = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(okClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                saaSAPIInterface = client.create(SaasAPIInterface.class);

            } catch (Exception e){
                Log.e(
                        "exception in RESTUtility: ",
                        e.getLocalizedMessage().toString() );
            }
        }
        return saaSAPIInterface;
    }

    public interface SaasAPIInterface {



        //Body value: InviteTargetUri=sip%3Aliben%40metio.onmicrosoft.com&WelcomeMessage=Welcome!&IsStart=true&Subject=HelpDesk&InvitedTargetDisplayName=Agent
        @POST("/GetAnonTokenJob")
        Call<SaaSResult> getAnonymousToken(
                @Body RequestBody body
        );

        @Headers("User-Agent: Retrofit2.0Tutorial-App")
        @POST("/IncomingMessagingBridgeJob")
        Call<retrofit2.Response> startIncomingMessageBridgeJob(
                @Body String body,
                Callback<retrofit2.Response> callback
        );

    }

}
