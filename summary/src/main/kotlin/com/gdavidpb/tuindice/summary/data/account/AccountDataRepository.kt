package com.gdavidpb.tuindice.summary.data.account

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.summary.data.account.source.LocalDataSource
import com.gdavidpb.tuindice.summary.data.account.source.RemoteDataSource
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository

class AccountDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val networkRepository: NetworkRepository
) : AccountRepository {
	override suspend fun getAccount(uid: String): Account {
		val isNetworkAvailable = networkRepository.isAvailable()
		val isCached = localDataSource.accountExists(uid)

		if (isNetworkAvailable && !isCached)
			remoteDataSource.getAccount()
				.also { account -> localDataSource.saveAccount(uid, account) }

		return localDataSource.getAccount(uid)
	}

	override suspend fun uploadProfilePicture(encodedPicture: String): ProfilePicture {
		return remoteDataSource.uploadProfilePicture(encodedPicture)
	}

	override suspend fun removeProfilePicture() {
		return remoteDataSource.removeProfilePicture()
	}
}