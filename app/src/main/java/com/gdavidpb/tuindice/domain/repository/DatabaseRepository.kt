package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import java.util.*

interface DatabaseRepository {
    suspend fun getAccount(uid: String, lastUpdate: Date): Account?

    suspend fun updateAuthData(uid: String, data: DstAuth)
    suspend fun updatePersonalData(uid: String, data: DstPersonal)
    suspend fun updateRecordData(uid: String, data: DstRecord)
    suspend fun updateToken(uid: String, token: String)

    suspend fun <T> remoteTransaction(transaction: suspend DatabaseRepository.() -> T): T
    suspend fun <T> localTransaction(transaction: suspend DatabaseRepository.() -> T): T
}