package com.gdavidpb.tuindice.enrollmentproof.data.repository

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository

class EnrollmentProofDataRepository(
	private val databaseDataSource: DatabaseDataSource,
	private val enrollmentProofApiDataSource: EnrollmentProofApiDataSource,
	private val storageDataSource: StorageDataSource,
	private val networkRepository: NetworkRepository
) : EnrollmentProofRepository {
	override suspend fun getEnrollmentProof(uid: String): EnrollmentProof {
		val currentQuarterName = databaseDataSource.getCurrentQuarterName(uid)
			?: throw EnrollmentProofNotFoundException()

		val isNetworkAvailable = networkRepository.isAvailable()
		val enrollmentProofExists = storageDataSource.enrollmentProofExists(uid, currentQuarterName)

		if (isNetworkAvailable && !enrollmentProofExists)
			enrollmentProofApiDataSource.getEnrollmentProof().also { enrollmentProof ->
				storageDataSource.saveEnrollmentProof(uid, currentQuarterName, enrollmentProof)
			}

		return storageDataSource.getEnrollmentProof(uid, currentQuarterName)
	}
}