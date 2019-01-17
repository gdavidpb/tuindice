package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.Enrollment
import com.gdavidpb.tuindice.domain.model.Record
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import okhttp3.ResponseBody

interface DstRepository {
    suspend fun getAccount(): Account?
    suspend fun getRecord(): Record?
    suspend fun getEnrollment(): Enrollment?
    suspend fun getEnrollmentProof(): ResponseBody?

    suspend fun auth(request: AuthRequest): AuthResponse?
}