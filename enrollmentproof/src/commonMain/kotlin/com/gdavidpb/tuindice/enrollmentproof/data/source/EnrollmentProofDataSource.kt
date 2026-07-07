package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentProofApiDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofOfflineException
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import kotlinx.coroutines.CancellationException

class EnrollmentProofDataSource(
	private val databaseDataSource: DatabaseDataRepository,
	private val enrollmentProofApiDataSource: EnrollmentProofApiDataRepository,
	private val storageDataSource: StorageDataRepository,
	private val networkRepository: NetworkRepository,
	private val credentialsRepository: CredentialsRepository
) : EnrollmentProofRepository {
	override suspend fun getEnrollmentProof(): EnrollmentProof {
		val currentQuarterName = databaseDataSource.getCurrentQuarterName()
			?: throw EnrollmentProofNotFoundException()

		val isNetworkAvailable = networkRepository.isAvailable()
		val enrollmentProofExists = storageDataSource.enrollmentProofExists(currentQuarterName)

		if (!isNetworkAvailable && !enrollmentProofExists) {
			throw EnrollmentProofOfflineException()
		}

		// With network the current document is always refetched; the cached file
		// is a fallback when the refetch fails, never a substitute for it.
		if (isNetworkAvailable) {
			runCatching {
				enrollmentProofApiDataSource.getEnrollmentProof(
					password = credentialsRepository.getPassword()
				).also { enrollmentProof ->
					storageDataSource.saveEnrollmentProof(currentQuarterName, enrollmentProof)
				}
			}.onFailure { exception ->
				if (exception is CancellationException || !enrollmentProofExists) throw exception
			}
		}

		return storageDataSource.getEnrollmentProof(currentQuarterName)
	}
}
