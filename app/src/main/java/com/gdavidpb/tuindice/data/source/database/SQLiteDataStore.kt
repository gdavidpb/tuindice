package com.gdavidpb.tuindice.data.source.database

import com.gdavidpb.tuindice.data.mapper.AccountEntityMapper
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import java.util.*

open class SQLiteDataStore(
        private val database: SQLiteDatabase,
        private val accountEntityMapper: AccountEntityMapper
) : LocalDatabaseRepository {
    override suspend fun activeAccount(email: String) {
        database.accounts.activeAccount(email)
    }

    override suspend fun getActiveAccount(): Account? {
        return database.accounts.getActive()?.let(accountEntityMapper::mapFrom)
    }

    override suspend fun removeActive() {
        return database.accounts.removeActive()
    }

    override suspend fun storeAccount(account: Account, active: Boolean) {
        return database.accounts.storeAccount(account.let(accountEntityMapper::mapTo).copy(active = active, lastUpdate = Date()))
    }
}