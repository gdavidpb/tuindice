package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Record

interface DatabaseRepository {
    suspend fun updateAccount(account: Account)
    suspend fun updateRecord(record: Record)
}