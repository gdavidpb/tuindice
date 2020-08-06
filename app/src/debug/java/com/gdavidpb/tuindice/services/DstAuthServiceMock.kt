package com.gdavidpb.tuindice.services

import com.gdavidpb.tuindice.data.source.service.DstAuthService
import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import com.gdavidpb.tuindice.services.responses.dstDefaultAuthResponse
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class DstAuthServiceMock(
        private val delegate: BehaviorDelegate<DstAuthService>
) : DstAuthService {
    override suspend fun auth(serviceUrl: String, usbId: String, password: String, eventId: String): Response<DstAuthResponseSelector> {
        val response = dstDefaultAuthResponse

        return delegate
                .returningResponse(response)
                .auth(serviceUrl, usbId, password, eventId)
    }
}