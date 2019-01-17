package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface DstEnrollmentService {
    @GET("secure/generarComprobante.do")
    fun getEnrollment(@Query("pagina") mode: String = "regular"): Call<DstEnrollmentDataSelector>

    @Streaming
    @GET("secure/imprimirComprobante.do")
    fun getEnrollmentProof(): Call<ResponseBody>
}