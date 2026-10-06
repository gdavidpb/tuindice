package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentSyncDataRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProofNotFoundReason
import kotlinx.coroutines.CancellationException

class EnrollmentSyncDataSource(
	private val syncStatusRepository: SyncStatusRepository,
	private val syncRepository: SyncRepository,
	private val credentialsRepository: CredentialsRepository
) : EnrollmentSyncDataRepository {
	override suspend fun getNotFoundReason(): EnrollmentProofNotFoundReason {
		val enrollment = lastSyncEnrollment()

		return when {
			enrollment?.situation != null -> EnrollmentProofNotFoundReason.Annulled
			enrollment?.status == SyncSourceStatus.NotEnrolled -> EnrollmentProofNotFoundReason.NotEnrolled
			else -> EnrollmentProofNotFoundReason.Unknown
		}
	}

	// A proof just fetched is the university saying the enrollment stands. When the last sync said
	// otherwise (annulled, or not enrolled) that sync is stale, and the record and the evaluations
	// would go on announcing what is no longer true until the next one: it is asked for right away,
	// past the cooldown. Best effort: the proof is already in hand, and a regular sync corrects it
	// anyway.
	override suspend fun refreshWhenContradicted() {
		val enrollment = lastSyncEnrollment()
		val deniedTheEnrollment = enrollment?.situation != null ||
			enrollment?.status == SyncSourceStatus.NotEnrolled

		if (!deniedTheEnrollment) return

		runCatching { credentialsRepository.getPassword() }
			.onFailure { throwable -> if (throwable is CancellationException) throw throwable }
			.onSuccess { password ->
				syncRepository.scheduleSync(password = password, policy = SyncPolicy.ForceRefresh)
			}
	}

	private suspend fun lastSyncEnrollment(): SyncSourceReport? {
		return runCatching { syncStatusRepository.getSyncReport().sources.enrollment }
			.onFailure { throwable -> if (throwable is CancellationException) throw throwable }
			.getOrNull()
	}
}
