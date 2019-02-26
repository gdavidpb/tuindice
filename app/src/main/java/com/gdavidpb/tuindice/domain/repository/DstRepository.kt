package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.Enrollment
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import okhttp3.ResponseBody

interface DstRepository {
    suspend fun getPersonalData(): DstPersonal?
    suspend fun getRecordData(): DstRecord?
    suspend fun getEnrollment(): Enrollment?
    suspend fun getEnrollmentProof(): ResponseBody?

    suspend fun auth(request: AuthRequest): AuthResponse?
}