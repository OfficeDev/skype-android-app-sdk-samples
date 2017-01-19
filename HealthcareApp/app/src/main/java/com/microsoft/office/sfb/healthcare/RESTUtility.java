/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office.sfb.healthcare;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;


public class RESTUtility {

    private  SaasAPIInterface saaSAPIInterface;
    private  String baseUrl;
    private okhttp3.OkHttpClient mOkClient;
    private Context mContext;

    public RESTUtility(Context context){
        mContext = context;
        baseUrl = mContext.getString(R.string.cloudAppBaseurl) ;
    }

    @SuppressLint("LongLogTag")
    private void buildLoggingInterceptor(){
        try {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            mOkClient = new okhttp3.OkHttpClient
                    .Builder()
                    .addInterceptor(new LoggingInterceptor())
                    .addInterceptor(httpLoggingInterceptor)
                    .build();

        } catch (Exception e) {
            Log.e(
                    "exception in RESTUtility: ",
                    e.getLocalizedMessage().toString() );
        }

    }
    @SuppressLint("LongLogTag")
    public  SaasAPIInterface getSaaSClient() {
        if (saaSAPIInterface == null) {

            try {

                if (mOkClient == null) {
                    buildLoggingInterceptor();
                }

                Retrofit SaaSClient = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(mOkClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                saaSAPIInterface = SaaSClient.create(SaasAPIInterface.class);

            } catch (Exception e){
                Log.e(
                        "exception in RESTUtility: ",
                        e.getLocalizedMessage().toString() );
            }
        }
        return saaSAPIInterface;
    }


    public interface SaasAPIInterface {



        @POST("/GetAnonTokenJob")
        Call<GetTokenResult> getAnonymousToken(
                @Body RequestBody body
        );

        @POST("/GetAdhocMeetingJob")
        Call<GetMeetingURIResult> getAdhocMeeting(
                @Body RequestBody body);

    }

    class LoggingInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {


            Request request = chain.request();
            request = request.newBuilder()

            .addHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
            .addHeader("Accept","text/plain, */*; q=0.01")
            .addHeader("Referer","https://sdksamplesucap.azurewebsites.net/")
            .addHeader("Accept-Language","en-US,en;q=0.8,zh-Hans-CN;q=0.5,zh-Hans;q=0.3")
            .addHeader("Origin","https://sdksamplesucap.azurewebsites.net")
            .addHeader("Accept-Encoding","gzip, deflate")
            .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
            .addHeader("Host",mContext.getString(R.string.SaasHostName))
            .addHeader("Content-Length",
                    String.valueOf(
                            chain.request()
                                    .body()
                                    .contentLength()))
            .addHeader("Connection","Keep-Alive")
            .addHeader("Cache-Control","no-cache")
                  .method(request.method(),request.body())
            .build();


            Response response = chain.proceed(request);
            return response;
        }
    }

}
