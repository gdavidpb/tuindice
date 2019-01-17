package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface DstAuthService {
    @FormUrlEncoded
    @POST("login")
    fun auth(@Query("service") serviceUrl: String,
             @Field("username") usbId: String,
             @Field("password") password: String,
             @Field("_eventId") eventId: String = "submit"): Call<DstAuthResponseSelector>
}