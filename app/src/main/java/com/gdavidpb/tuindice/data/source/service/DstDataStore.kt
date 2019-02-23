package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.exception.AuthException
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.toAccount
import com.gdavidpb.tuindice.utils.toAuthResponse
import com.gdavidpb.tuindice.utils.toEnrollment
import com.gdavidpb.tuindice.utils.toRecord
import okhttp3.ResponseBody
import retrofit2.HttpException

open class DstDataStore(
        private val authService: DstAuthService,
        private val recordService: DstRecordService,
        private val enrollmentService: DstEnrollmentService
) : DstRepository {
    override suspend fun getPersonal(): Account? {
        val response = recordService.getPersonalData().execute().body()

        return response?.toAccount()
    }

    override suspend fun getRecord(): Record? {
        val response = recordService.getRecordData().execute().body()

        return response?.toRecord()
    }

    override suspend fun getEnrollment(): Enrollment? {
        val response = enrollmentService.getEnrollment().execute().body()

        return response?.toEnrollment()
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
                val authResponse = it.toAuthResponse(request = request)

                when (authResponse.code) {
                    AuthResponseCode.SUCCESS, AuthResponseCode.NO_ENROLLED -> authResponse
                    else -> throw AuthException(code = authResponse.code, message = authResponse.message)
                }
            }
        } else
            throw HttpException(response)
    }
}