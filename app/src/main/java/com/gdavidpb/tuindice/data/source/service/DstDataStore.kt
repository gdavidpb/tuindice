package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.mapper.AccountSelectorMapper
import com.gdavidpb.tuindice.data.mapper.AuthResponseMapper
import com.gdavidpb.tuindice.data.mapper.EnrollmentMapper
import com.gdavidpb.tuindice.data.mapper.RecordMapper
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.exception.AuthException
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.google.common.net.MediaType
import okhttp3.ResponseBody

open class DstDataStore(
        private val authService: DstAuthService,
        private val recordService: DstRecordService,
        private val enrollmentService: DstEnrollmentService,
        private val authResponseMapper: AuthResponseMapper,
        private val accountSelectorMapper: AccountSelectorMapper,
        private val recordMapper: RecordMapper,
        private val enrollmentMapper: EnrollmentMapper
) : DstRepository {
    override suspend fun getAccount(): Account {
        return recordService.getPersonalData().let(accountSelectorMapper::map)
    }

    override suspend fun getRecord(): Record {
        return recordService.getRecordData().let(recordMapper::map)
    }

    override suspend fun getEnrollment(): Enrollment {
        return enrollmentService.getEnrollment().let(enrollmentMapper::map)
    }

    override suspend fun getEnrollmentProof(): ResponseBody? {
        return enrollmentService.getEnrollmentProof().let {
            val isValid = "${it.contentType()}" == "${MediaType.PDF}"

            if (isValid) it else null
        }
    }

    override suspend fun auth(request: AuthRequest): AuthResponse {
        return authService.auth(request.serviceUrl, request.usbId, request.password).let {
            val authResponse = it.let(authResponseMapper::map).copy(request = request)

            when (authResponse.code) {
                AuthResponseCode.SUCCESS, AuthResponseCode.NO_ENROLLED -> authResponse
                else -> throw AuthException(code = authResponse.code, message = authResponse.message)
            }
        }
    }
}