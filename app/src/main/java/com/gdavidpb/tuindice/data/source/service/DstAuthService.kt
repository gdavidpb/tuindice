package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.selectors.DstAuthResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface DstAuthService {
    @FormUrlEncoded
    @POST("login")
    suspend fun auth(
            @Query("service") serviceUrl: String,
            @Field("username") usbId: String,
            @Field("password") password: String,
            @Field("_eventId") eventId: String = "submit"
    ): Response<DstAuthResponse>
}