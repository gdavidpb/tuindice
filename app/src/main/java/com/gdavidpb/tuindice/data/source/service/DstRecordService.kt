package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import retrofit2.Call
import retrofit2.http.GET

interface DstRecordService {
    @GET("datosPersonales.do")
    fun getPersonalData(): Call<DstPersonalDataSelector>

    @GET("informeAcademico.do")
    fun getRecordData(): Call<DstRecordDataSelector>
}