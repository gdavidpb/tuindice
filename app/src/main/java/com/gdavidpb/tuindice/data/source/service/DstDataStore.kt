package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.SignInResponse
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.usecase.request.SignInRequest
import com.gdavidpb.tuindice.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.utils.mappers.toAuthResponse
import com.gdavidpb.tuindice.utils.mappers.toEnrollment
import com.gdavidpb.tuindice.utils.mappers.toPersonalData
import com.gdavidpb.tuindice.utils.mappers.toRecord
import okhttp3.ResponseBody
import java.io.StreamCorruptedException

open class DstDataStore(
        private val authService: DstAuthService,
        private val recordService: DstRecordService,
        private val enrollmentService: DstEnrollmentService
) : DstRepository {
    override suspend fun getPersonalData(): DstPersonal {
        return recordService.getPersonalData()
                .getOrThrow()
                .toPersonalData()
    }

    override suspend fun getRecordData(): DstRecord {
        return recordService.getRecordData()
                .getOrThrow()
                .toRecord()
    }

    override suspend fun getEnrollment(): DstEnrollment {
        return enrollmentService.getEnrollment()
                .getOrThrow()
                .toEnrollment()
    }

    override suspend fun getEnrollmentProof(): ResponseBody {
        return enrollmentService.getEnrollmentProof()
                .getOrThrow()
                .also { response ->
                    check("${response.contentType()}" == "application/pdf") {
                        throw StreamCorruptedException()
                    }
                }
    }

    override suspend fun signIn(request: SignInRequest): SignInResponse {
        return authService.auth(request.serviceUrl, request.usbId, request.password)
                .getOrThrow()
                .toAuthResponse()
                .also { response ->
                    check((response.code == AuthResponseCode.SUCCESS) || (response.code == AuthResponseCode.NOT_ENROLLED)) {
                        throw AuthenticationException(code = response.code, message = response.message)
                    }
                }
    }
}