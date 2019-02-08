package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account

interface DatabaseRepository {
    suspend fun setToken(token: String)
    suspend fun updateAccount(account: Account)
}