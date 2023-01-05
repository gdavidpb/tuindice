package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

interface AccountRepository {
	suspend fun getAccount(uid: String): Account

	suspend fun uploadProfilePicture(encodedPicture: String): ProfilePicture
	suspend fun removeProfilePicture()
}