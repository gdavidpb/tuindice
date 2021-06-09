package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.domain.model.AuthErrorCode
import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.SignInRequest
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.utils.mappers.*
import okhttp3.ResponseBody
import java.io.StreamCorruptedException

class DstDataSource(
        private val authService: DstAuthService,
        private val recordService: DstRecordService,
        private val enrollmentService: DstEnrollmentService
) : DstRepository {
    override suspend fun signIn(signInRequest: SignInRequest, serviceUrl: String): DstAuth {
        val usbId = signInRequest.usbId.asUsbId()

        return authService.auth(
            serviceUrl = serviceUrl,
            usbId = usbId,
            password = signInRequest.password
        )
            .getOrThrow()
            .toAuth()
            .also { response ->
                check(response.code != AuthResponseCode.INVALID_CREDENTIALS) {
                    throw AuthenticationException(
                        errorCode = AuthErrorCode.INVALID_CREDENTIALS,
                        message = response.message
                    )
                }

                check(response.code != AuthResponseCode.SESSION_EXPIRED) {
                    throw AuthenticationException(
                        errorCode = AuthErrorCode.SESSION_EXPIRED,
                        message = response.message
                    )
                }
            }
    }

    override suspend fun getPersonalData(): DstPersonal {
        return recordService.getPersonalData()
                .getOrThrow()
                .toPersonal()
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
}