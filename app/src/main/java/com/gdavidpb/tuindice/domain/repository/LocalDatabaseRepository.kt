package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account

interface LocalDatabaseRepository {
    suspend fun getActiveAccount(): Account?
    suspend fun removeActive()
    suspend fun storeAccount(account: Account, active: Boolean)
}