package com.gdavidpb.tuindice.services

import com.gdavidpb.tuindice.data.source.service.DstRecordService
import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.services.responses.defaultPersonalResponse
import com.gdavidpb.tuindice.services.responses.defaultRecordResponse
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class DstRecordServiceMock(
        private val delegate: BehaviorDelegate<DstRecordService>
) : DstRecordService {
    override suspend fun getPersonalData(): Response<DstPersonalDataSelector> {
        val response = defaultPersonalResponse

        return delegate
                .returningResponse(response)
                .getPersonalData()
    }

    override suspend fun getRecordData(): Response<DstRecordDataSelector> {
        val response = defaultRecordResponse

        return delegate
                .returningResponse(response)
                .getRecordData()
    }
}