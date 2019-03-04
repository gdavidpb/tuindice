package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord

interface DatabaseRepository {
    suspend fun getAccountByUId(uid: String): Account
    suspend fun getActiveAccount(): Account?

    suspend fun updateAuthData(data: DstAuth)
    suspend fun updatePersonalData(data: DstPersonal)
    suspend fun updateRecordData(data: DstRecord)
    suspend fun updateToken(token: String)

    suspend fun <T> networkTransaction(transaction: suspend DatabaseRepository.() -> T): T
}