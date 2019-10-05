package com.example.myapplication.youtube_model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeApi {
    /**
     * this interface represents server in our app
     */
    @GET("search")
    Call<Example> getParentObject(
            @Query("part") String part,
            @Query("q") String q,
            @Query("type") String type,
            @Query("key") String key

    );

}
