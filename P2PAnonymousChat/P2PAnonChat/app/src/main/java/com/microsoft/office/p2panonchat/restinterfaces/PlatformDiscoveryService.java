package com.microsoft.office.p2panonchat.restinterfaces;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by johnau on 9/8/2016.
 */
public interface PlatformDiscoveryService {

    @GET("/platformService/discover")
    void getUCWAAnonApplication(
            @Query("$anonymousContext") String token,
            Callback<Response> callback
    );
}
