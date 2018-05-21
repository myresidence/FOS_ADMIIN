package com.hfad.fosadmin.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by icefrog on 4/8/18.
 */

public interface IGeoCoordinates {

    @GET("maps/api/geocode/json")
    Call<String> getGeoCode(@Query("address") String address);

    @GET("maps/api/directions/json")
    Call<String> getDirections(@Query("origin") String origin,@Query("destination") String destination);

}
