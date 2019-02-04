package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account

interface DatabaseRepository {
    suspend fun setToken()
    suspend fun updateAccount(account: Account)
}