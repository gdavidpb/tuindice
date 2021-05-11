package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.service.*
import okhttp3.ResponseBody

interface DstRepository {
    suspend fun signIn(credentials: DstCredentials): DstAuth
    suspend fun checkCredentials(credentials: DstCredentials)

    suspend fun getPersonalData(): DstPersonal
    suspend fun getRecordData(): DstRecord
    suspend fun getEnrollment(): DstEnrollment
    suspend fun getEnrollmentProof(): ResponseBody
}