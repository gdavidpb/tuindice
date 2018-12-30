package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import io.reactivex.Completable
import io.reactivex.Maybe

interface LocalDatabaseRepository {
    fun getActiveAccount(): Maybe<Account>
    fun removeActive(): Completable
    fun storeAccount(account: Account, active: Boolean): Completable
}