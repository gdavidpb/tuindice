package com.gdavidpb.tuindice.services

import com.gdavidpb.tuindice.data.source.service.DstUsbIdService
import com.gdavidpb.tuindice.data.source.service.responses.DstUsbIdCheckResponse
import com.gdavidpb.tuindice.services.responses.defaultDstUsbIdCheckResponse
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class DstUsbIdServiceMock(
        private val delegate: BehaviorDelegate<DstUsbIdService>
) : DstUsbIdService {
    override suspend fun checkCredentials(usbId: String, password: String): Response<DstUsbIdCheckResponse> {
        val response = defaultDstUsbIdCheckResponse

        return delegate
                .returningResponse(response)
                .checkCredentials(usbId, password)
    }
}