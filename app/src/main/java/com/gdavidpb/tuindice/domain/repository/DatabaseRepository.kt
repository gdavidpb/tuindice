package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord

interface DatabaseRepository {
    suspend fun updateAuthData(data: DstAuth)
    suspend fun updatePersonalData(data: DstPersonal)
    suspend fun updateRecordData(data: DstRecord)
}