package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.mapper.AccountSelectorMapper
import com.gdavidpb.tuindice.data.mapper.AuthResponseMapper
import com.gdavidpb.tuindice.data.mapper.EnrollmentMapper
import com.gdavidpb.tuindice.data.mapper.RecordMapper
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.exception.AuthException
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import okhttp3.ResponseBody
import retrofit2.HttpException

open class DstDataStore(
        private val authService: DstAuthService,
        private val recordService: DstRecordService,
        private val enrollmentService: DstEnrollmentService,
        private val authResponseMapper: AuthResponseMapper,
        private val accountSelectorMapper: AccountSelectorMapper,
        private val recordMapper: RecordMapper,
        private val enrollmentMapper: EnrollmentMapper
) : DstRepository {
    override suspend fun getAccount(): Account? {
        val response = recordService.getPersonalData().execute().body()

        return response?.let(accountSelectorMapper::map)
    }

    override suspend fun getRecord(): Record? {
        val response = recordService.getRecordData().execute().body()

        return response?.let(recordMapper::map)
    }

    override suspend fun getEnrollment(): Enrollment? {
        val response = enrollmentService.getEnrollment().execute().body()

        return response?.let(enrollmentMapper::map)
    }

    override suspend fun getEnrollmentProof(): ResponseBody? {
        val response = enrollmentService.getEnrollmentProof().execute().body()

        return response?.let {
            val isValid = "${it.contentType()}" == "application/pdf"

            if (isValid) it else null
        }
    }

    override suspend fun auth(request: AuthRequest): AuthResponse? {
        val response = authService.auth(request.serviceUrl, request.usbId, request.password).execute()

        return if (response.isSuccessful) {
            response.body()?.let {
                val authResponse = it.let(authResponseMapper::map).copy(request = request)

                when (authResponse.code) {
                    AuthResponseCode.SUCCESS, AuthResponseCode.NO_ENROLLED -> authResponse
                    else -> throw AuthException(code = authResponse.code, message = authResponse.message)
                }
            }
        } else
            throw HttpException(response)
    }
}