package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isServerError
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentProofApiDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentSyncDataRepository
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
	private val credentialsRepository: CredentialsRepository,
	private val enrollmentSyncDataSource: EnrollmentSyncDataRepository
) : EnrollmentProofRepository {
	override suspend fun getEnrollmentProof(): EnrollmentProof {
		val currentQuarterName = databaseDataSource.getCurrentQuarterName()
			?: throw EnrollmentProofNotFoundException(reason = enrollmentSyncDataSource.getNotFoundReason())

		val isNetworkAvailable = networkRepository.isAvailable()
		val enrollmentProofExists = storageDataSource.enrollmentProofExists(currentQuarterName)

		if (!isNetworkAvailable && !enrollmentProofExists) {
			throw EnrollmentProofOfflineException()
		}

		// With network the current document is always refetched; the cached file is a fallback
		// for a failure that is only transient, never a substitute for the answer the server gave.
		val isFresh = isNetworkAvailable && refresh(currentQuarterName, enrollmentProofExists)

		// The university has just given a proof: if the last sync said there was no enrollment to
		// prove, it is that sync that is out of date.
		if (isFresh) enrollmentSyncDataSource.refreshWhenContradicted()

		return storageDataSource
			.getEnrollmentProof(currentQuarterName)
			.copy(isFromCache = !isFresh)
	}

	/** True when the saved file is now the fresh one; false when it is kept because the refetch failed transiently. */
	private suspend fun refresh(quarterName: String, enrollmentProofExists: Boolean): Boolean {
		val failure = runCatching {
			enrollmentProofApiDataSource.getEnrollmentProof(
				password = credentialsRepository.getPassword()
			).also { enrollmentProof ->
				storageDataSource.saveEnrollmentProof(quarterName, enrollmentProof)
			}
		}.exceptionOrNull() ?: return true

		if (failure is CancellationException) throw failure

		// A 404 is the answer (the university has no proof to give), so it never falls back to a
		// saved copy; the same goes for a 409, which sends the user to update the password.
		val isNotFound = failure.isNotFound()

		if (isNotFound || !enrollmentProofExists || !failure.isTransient()) {
			throw if (isNotFound) {
				EnrollmentProofNotFoundException(
					reason = enrollmentSyncDataSource.getNotFoundReason(),
					cause = failure
				)
			} else {
				failure
			}
		}

		return false
	}

	private fun Throwable.isTransient(): Boolean {
		return isUnavailable() || isServerError() || isTimeout() || isConnection()
	}
}
