package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import io.reactivex.Maybe
import io.reactivex.Single
import retrofit2.http.GET

interface DstRecordService {
    @GET("datosPersonales.do")
    fun getPersonalData(): Single<DstPersonalDataSelector>

    @GET("informeAcademico.do")
    fun getRecordData(): Maybe<DstRecordDataSelector>
}