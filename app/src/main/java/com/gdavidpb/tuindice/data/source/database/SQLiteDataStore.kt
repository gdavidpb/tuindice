package com.gdavidpb.tuindice.data.source.database

import com.gdavidpb.tuindice.data.mapper.AccountEntityMapper
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import io.reactivex.Completable
import io.reactivex.Maybe
import java.util.*

open class SQLiteDataStore(
        private val database: SQLiteDatabase,
        private val accountEntityMapper: AccountEntityMapper
) : LocalDatabaseRepository {
    override fun storeAccount(account: Account, active: Boolean): Completable {
        return database.accounts.storeAccount(account.let(accountEntityMapper::mapTo).copy(active = active, lastUpdate = Date().time))
    }

    override fun getActiveAccount(): Maybe<Account> {
        return database.accounts.getActive().map(accountEntityMapper::mapFrom)
    }

    override fun removeActive(): Completable {
        return Completable.fromCallable(database.accounts::removeActive)
    }
}