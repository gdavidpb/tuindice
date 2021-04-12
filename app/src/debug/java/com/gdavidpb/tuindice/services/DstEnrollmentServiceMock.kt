package com.gdavidpb.tuindice.services

import android.content.res.Resources
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.service.DstEnrollmentService
import com.gdavidpb.tuindice.data.source.service.selectors.DstEnrollmentResponse
import com.gdavidpb.tuindice.services.responses.defaultEnrollmentResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class DstEnrollmentServiceMock(
        private val delegate: BehaviorDelegate<DstEnrollmentService>
) : DstEnrollmentService, KoinComponent {
    private val resources by inject<Resources>()

    override suspend fun getEnrollment(mode: String): Response<DstEnrollmentResponse> {
        val response = defaultEnrollmentResponse

        return delegate
                .returningResponse(response)
                .getEnrollment(mode)
    }

    override suspend fun getEnrollmentProof(): Response<ResponseBody> {
        val enrollment = resources.openRawResource(R.raw.enrollment_mock).use { it.readBytes() }
        val response = enrollment.toResponseBody("application/pdf".toMediaTypeOrNull())

        return delegate
                .returningResponse(response)
                .getEnrollmentProof()
    }
}