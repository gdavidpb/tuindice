package com.gdavidpb.tuindice.services

import com.gdavidpb.tuindice.data.source.service.DstRecordService
import com.gdavidpb.tuindice.data.source.service.responses.DstPersonalResponse
import com.gdavidpb.tuindice.data.source.service.responses.DstRecordResponse
import com.gdavidpb.tuindice.services.responses.defaultPersonalResponse
import com.gdavidpb.tuindice.services.responses.defaultRecordResponse
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class DstRecordServiceMock(
        private val delegate: BehaviorDelegate<DstRecordService>
) : DstRecordService {
    override suspend fun getPersonalData(): Response<DstPersonalResponse> {
        return delegate
                .returningResponse(defaultPersonalResponse)
                .getPersonalData()
    }

    override suspend fun getRecordData(): Response<DstRecordResponse> {
        val response = defaultRecordResponse

        return delegate
                .returningResponse(response)
                .getRecordData()
    }
}