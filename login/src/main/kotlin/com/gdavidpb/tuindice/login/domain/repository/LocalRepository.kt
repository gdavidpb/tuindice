package com.gdavidpb.tuindice.login.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Account

interface LocalRepository {
	suspend fun getAccount(uid: String): Account
}