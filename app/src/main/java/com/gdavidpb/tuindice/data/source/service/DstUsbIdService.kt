package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.responses.DstUsbIdCheckResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DstUsbIdService {
    @FormUrlEncoded
    @POST("check_user.py")
    suspend fun checkCredentials(
            @Field("uid") usbId: String,
            @Field("pwd") password: String
    ): Response<DstUsbIdCheckResponse>
}