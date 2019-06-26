package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.exception.AuthException
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.*
import okhttp3.ResponseBody
import java.net.InetAddress
import java.net.Socket
import java.net.URL

open class DstDataStore(
        private val authService: DstAuthService,
        private val recordService: DstRecordService,
        private val enrollmentService: DstEnrollmentService
) : DstRepository {
    override suspend fun getPersonalData(): DstPersonal? {
        return recordService.getPersonalData().await()?.toPersonalData()
    }

    override suspend fun getRecordData(): DstRecord? {
        return recordService.getRecordData().await()?.toRecord()
    }

    override suspend fun getEnrollment(): DstEnrollment? {
        return runCatching {
            enrollmentService.getEnrollment().await()
        }.getOrNull()?.toEnrollment()
    }

    override suspend fun getEnrollmentProof(): ResponseBody? {
        return enrollmentService.getEnrollmentProof().await()?.let {
            val isValid = "${it.contentType()}" == "application/pdf"

            if (isValid) it else null
        }
    }

    override suspend fun auth(request: AuthRequest): AuthResponse? {
        return authService.auth(request.serviceUrl, request.usbId, request.password).await()?.let {
            val authResponse = it.toAuthResponse()

            when (authResponse.code) {
                AuthResponseCode.SUCCESS, AuthResponseCode.NO_ENROLLED -> authResponse
                else -> throw AuthException(code = authResponse.code, message = authResponse.message)
            }
        }
    }

    override suspend fun ping(serviceUrl: String): Boolean {
        return runCatching {
            val url = URL(serviceUrl)
            val hostAddress = InetAddress.getByName(url.host).hostAddress
            val socket = Socket(hostAddress, 80).apply {
                soTimeout = 10000
            }
            socket.close()
            true
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            it
        }.getOrDefault(false)
    }
}