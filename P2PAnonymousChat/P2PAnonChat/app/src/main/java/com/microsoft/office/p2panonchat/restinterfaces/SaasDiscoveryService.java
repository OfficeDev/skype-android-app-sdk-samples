package com.microsoft.office.p2panonchat.restinterfaces;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by johnau on 9/8/2016.
 */
public interface SaasDiscoveryService {

    @POST("/GetAnonTokenJob")
    void getAnonymousToken(
            @Body String body,
            Callback<Response> callback
    );

    @POST("/IncomingMessagingBridgeJob")
    void startIncomingMessageBridgeJob(
            Callback<Response> callback
    );
}
