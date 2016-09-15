package com.microsoft.office.p2panonchat;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
//import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;



public class RESTUtility {

    private  SaasAPIInterface saaSAPIInterface;
    private  String baseUrl = "https://metiobank.cloudapp.net/GetAnonTokenJob/" ;

    @SuppressLint("LongLogTag")
    public  SaasAPIInterface getClient() {
        if (saaSAPIInterface == null) {

            try {
//                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                okhttp3.OkHttpClient okClient = new okhttp3.OkHttpClient
                        .Builder()
                        .addInterceptor(new LoggingInterceptor())
//                        .addInterceptor(httpLoggingInterceptor)
                        .build();

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
    class LoggingInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {


            BufferedSink bufferedSink = new Buffer();

            chain.request().body().writeTo(bufferedSink);

            String bodyString = bufferedSink.toString();


            Request request = chain.request();
            request = request.newBuilder()
                    .addHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("Accept","text/plain, */*; q=0.01")
                    .addHeader("Referer","https://sdksamplesucap.azurewebsites.net/")
                    .addHeader("Accept-Language","en-US,en;q=0.8,zh-Hans-CN;q=0.5,zh-Hans;q=0.3")
                    .addHeader("Origin","https://sdksamplesucap.azurewebsites.net")
                    .addHeader("Accept-Encoding","gzip, deflate")
                    .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
                    .addHeader("Host","metiobank.cloudapp.net")
                    .addHeader("Content-Length",
                            String.valueOf(
                                    chain.request()
                                            .body()
                                            .contentLength()))
                    .addHeader("Connection","Keep-Alive")
                    .addHeader("Cache-Control","no-cache")
                    .build();


            Log.d("OkHttp", String.format("Sending request %s on %s%s",
            request.url(), bodyString, request.headers()));

            Response response = chain.proceed(request);

            Log.d("OkHttp", String.format("Received response for %s headers: %s body: %s",
                    response.request().url(),response.headers(),response.body().string()));
            return response;
        }
    }

}
