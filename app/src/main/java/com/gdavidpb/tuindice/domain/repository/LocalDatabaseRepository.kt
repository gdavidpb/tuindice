package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import io.reactivex.Maybe
import io.reactivex.Single

interface LocalDatabaseRepository {
    fun getActiveAccount(): Maybe<Account>
}