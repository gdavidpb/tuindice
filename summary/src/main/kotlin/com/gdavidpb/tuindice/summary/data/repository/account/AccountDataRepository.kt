package com.gdavidpb.tuindice.summary.data.repository.account

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class AccountDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource
) : AccountRepository {
	override suspend fun getAccountStream(uid: String): Flow<Account> {
		return localDataSource.getAccount(uid)
			.distinctUntilChanged()
			.transform { localAccount ->
				val isOnCooldown = settingsDataSource.isGetAccountOnCooldown()

				if (localAccount != null)
					emit(localAccount)

				if (!isOnCooldown) {
					val remoteAccount = remoteDataSource.getAccount()

					localDataSource.saveAccount(uid, remoteAccount)

					settingsDataSource.setGetAccountOnCooldown()

					emit(remoteAccount)
				}
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