package com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.LocalDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.RemoteDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository

class EnrollmentProofDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val storageDataSource: StorageDataSource,
	private val networkRepository: NetworkRepository
) : EnrollmentProofRepository {
	override suspend fun getEnrollmentProof(uid: String): EnrollmentProof? {
		val currentQuarter = localDataSource.getCurrentQuarter(uid) ?: return null

		val isNetworkAvailable = networkRepository.isAvailable()
		val enrollmentProofExists = storageDataSource.enrollmentProofExists(uid, currentQuarter)

		if (isNetworkAvailable && !enrollmentProofExists)
			remoteDataSource.getEnrollmentProof().also { enrollmentProof ->
				storageDataSource.saveEnrollmentProof(uid, currentQuarter, enrollmentProof)
			}

		return storageDataSource.getEnrollmentProof(uid, currentQuarter)
	}
}