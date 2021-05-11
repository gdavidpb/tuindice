package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.domain.model.AuthErrorCode
import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.utils.mappers.*
import okhttp3.ResponseBody
import java.io.StreamCorruptedException

open class DstDataSource(
        private val authService: DstAuthService,
        private val usbIdService: DstUsbIdService,
        private val recordService: DstRecordService,
        private val enrollmentService: DstEnrollmentService
) : DstRepository {
    override suspend fun signIn(credentials: DstCredentials): DstAuth {
        val usbId = credentials.usbId.asUsbId()

        return authService.auth(
            serviceUrl = credentials.serviceUrl,
            usbId = usbId,
            password = credentials.password
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

    override suspend fun checkCredentials(credentials: DstCredentials) {
        val usbId = credentials.usbId.asUsbId()

        usbIdService.checkCredentials(
            usbId = usbId,
            password = credentials.password
        )
            .getOrThrow()
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