package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.selectors.DstPersonalResponse
import com.gdavidpb.tuindice.data.source.service.selectors.DstRecordResponse
import retrofit2.Response
import retrofit2.http.GET

interface DstRecordService {
    @GET("datosPersonales.do")
    suspend fun getPersonalData(): Response<DstPersonalResponse>

    @GET("informeAcademico.do")
    suspend fun getRecordData(): Response<DstRecordResponse>
}