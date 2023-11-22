package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
	suspend fun getAccountStream(uid: String): Flow<Account>

	suspend fun uploadProfilePicture(uid: String, encodedPicture: String): ProfilePicture
	suspend fun removeProfilePicture(uid: String)
}