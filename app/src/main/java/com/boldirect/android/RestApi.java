package com.boldirect.android;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by Rashida on 28/05/17.
 */

public interface RestApi {

    @GET("api/")
    Call<ApiResponse> getApiVersion(@Header("Authorization") String authorization);
}
