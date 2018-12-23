package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface DstEnrollmentService {
    @GET("secure/generarComprobante.do")
    fun getEnrollment(@Query("pagina") mode: String = "regular"): Maybe<DstEnrollmentDataSelector>

    @Streaming
    @GET("secure/imprimirComprobante.do")
    fun getEnrollmentProof(): Single<ResponseBody>
}