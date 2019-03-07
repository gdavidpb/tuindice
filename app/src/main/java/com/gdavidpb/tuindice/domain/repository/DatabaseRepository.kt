package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import java.util.*

interface DatabaseRepository {
    suspend fun getActiveAccount(lastUpdate: Date): Account?
    suspend fun getAccountByUId(uid: String, lastUpdate: Date): Account

    suspend fun updateAuthData(data: DstAuth)
    suspend fun updatePersonalData(data: DstPersonal)
    suspend fun updateRecordData(data: DstRecord)
    suspend fun updateToken(token: String)

    suspend fun <T> remoteTransaction(transaction: suspend DatabaseRepository.() -> T): T
    suspend fun <T> localTransaction(transaction: suspend DatabaseRepository.() -> T): T
}