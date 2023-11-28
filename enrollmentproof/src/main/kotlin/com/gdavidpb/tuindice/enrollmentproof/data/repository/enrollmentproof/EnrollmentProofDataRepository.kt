package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository

class EnrollmentProofDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val storageDataSource: StorageDataSource,
	private val networkRepository: NetworkRepository
) : EnrollmentProofRepository {
	override suspend fun getEnrollmentProof(uid: String): EnrollmentProof? {
		val currentQuarterName = localDataSource.getCurrentQuarterName(uid) ?: return null

		val isNetworkAvailable = networkRepository.isAvailable()
		val enrollmentProofExists = storageDataSource.enrollmentProofExists(uid, currentQuarterName)

		if (isNetworkAvailable && !enrollmentProofExists)
			remoteDataSource.getEnrollmentProof().also { enrollmentProof ->
				storageDataSource.saveEnrollmentProof(uid, currentQuarterName, enrollmentProof)
			}

		return storageDataSource.getEnrollmentProof(uid, currentQuarterName)
	}
}