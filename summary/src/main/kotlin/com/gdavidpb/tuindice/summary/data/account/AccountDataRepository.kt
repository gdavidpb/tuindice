package com.gdavidpb.tuindice.summary.data.account

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.data.account.source.LocalDataSource
import com.gdavidpb.tuindice.summary.data.account.source.RemoteDataSource
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class AccountDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource
) : AccountRepository {
	override suspend fun getAccount(uid: String): Flow<Account> {
		return localDataSource.getAccount(uid)
			.distinctUntilChanged()
			.transform { account ->
				val isOnCooldown = localDataSource.isGetAccountOnCooldown()

				if (account != null && !isOnCooldown)
					emit(account)
				else
					emit(remoteDataSource.getAccount().also { response ->
						localDataSource.saveAccount(uid, response)
						localDataSource.setGetAccountCooldown()
					})
			}
	}

	override suspend fun uploadProfilePicture(uid: String, encodedPicture: String): ProfilePicture {
		return remoteDataSource.uploadProfilePicture(encodedPicture).also { profilePicture ->
			localDataSource.saveProfilePicture(uid = uid, url = profilePicture.url)
		}
	}

	override suspend fun removeProfilePicture(uid: String) {
		remoteDataSource.removeProfilePicture()
		localDataSource.saveProfilePicture(uid = uid, url = "")
	}
}