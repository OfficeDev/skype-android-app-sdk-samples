/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office.p2panonchat.restinterfaces;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface PlatformDiscoveryService {

    @GET("/platformService/discover")
    void getUCWAAnonApplication(
            @Query("$anonymousContext") String token,
            Callback<Response> callback
    );
}
