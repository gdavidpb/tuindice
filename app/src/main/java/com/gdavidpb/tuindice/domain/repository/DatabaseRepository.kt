package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstData

interface DatabaseRepository {
    suspend fun getAccount(uid: String): Account?

    suspend fun getCurrentQuarter(uid: String): Quarter?
    suspend fun getQuarters(uid: String): List<Quarter>

    suspend fun updateSubject(subject: Subject)

    suspend fun updateToken(uid: String, token: String)
    suspend fun updateAuthData(uid: String, data: DstAuth)
    suspend fun updateData(uid: String, data: Collection<DstData>)

    suspend fun <T> remoteTransaction(transaction: suspend DatabaseRepository.() -> T): T
    suspend fun <T> localTransaction(transaction: suspend DatabaseRepository.() -> T): T
}