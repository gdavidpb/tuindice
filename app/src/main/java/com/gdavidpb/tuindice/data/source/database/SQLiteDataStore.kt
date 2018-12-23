package com.gdavidpb.tuindice.data.source.database

import com.gdavidpb.tuindice.data.mapper.AccountEntityMapper
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import io.reactivex.Maybe

open class SQLiteDataStore(
        private val database: SQLiteDatabase,
        private val accountEntityMapper: AccountEntityMapper
) : LocalDatabaseRepository {
    override fun getActiveAccount(): Maybe<Account> = database.accounts.getActive().map(accountEntityMapper::map)
}