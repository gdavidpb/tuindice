package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import okhttp3.ResponseBody

interface DstRepository {
    suspend fun getPersonalData(): DstPersonal?
    suspend fun getRecordData(): DstRecord?
    suspend fun getEnrollment(): DstEnrollment?
    suspend fun getEnrollmentProof(): ResponseBody?

    suspend fun ping(serviceUrl: String): Boolean

    suspend fun auth(request: AuthRequest): AuthResponse?
}