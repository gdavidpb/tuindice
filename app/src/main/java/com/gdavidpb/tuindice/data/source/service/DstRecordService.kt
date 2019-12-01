package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import retrofit2.Response
import retrofit2.http.GET

interface DstRecordService {
    @GET("datosPersonales.do")
    suspend fun getPersonalData(): Response<DstPersonalDataSelector>

    @GET("informeAcademico.do")
    suspend fun getRecordData(): Response<DstRecordDataSelector>
}