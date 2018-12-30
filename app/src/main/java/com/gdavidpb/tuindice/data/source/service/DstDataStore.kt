package com.gdavidpb.tuindice.data.source.service

import android.util.Log
import com.gdavidpb.tuindice.data.mapper.AccountSelectorMapper
import com.gdavidpb.tuindice.data.mapper.AuthResponseMapper
import com.gdavidpb.tuindice.data.mapper.EnrollmentMapper
import com.gdavidpb.tuindice.data.mapper.RecordMapper
import com.gdavidpb.tuindice.data.model.exception.AuthException
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.google.common.net.MediaType
import io.reactivex.Maybe
import io.reactivex.Single
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
    override fun getAccount(): Single<Account> {
        return recordService.getPersonalData().map(accountSelectorMapper::map)
    }

    override fun getRecord(): Maybe<Record> {
        return recordService.getRecordData().map(recordMapper::map)
    }

    override fun getEnrollment(): Maybe<Enrollment> {
        return enrollmentService.getEnrollment().map(enrollmentMapper::map)
    }

    override fun getEnrollmentProof(): Maybe<ResponseBody> {
        return enrollmentService.getEnrollmentProof().filter {
            "${it.contentType()}" == "${MediaType.PDF}"
        }
    }

    override fun auth(request: AuthRequest): Single<AuthResponse> {
        return authService.auth(request.serviceUrl, request.usbId, request.password).map {
            val authResponse = it.let(authResponseMapper::map).copy(request = request)

            when (authResponse.code) {
                AuthResponseCode.SUCCESS, AuthResponseCode.NO_ENROLLED -> authResponse
                else -> throw AuthException(code = authResponse.code, message = authResponse.message)
            }
        }
    }
}