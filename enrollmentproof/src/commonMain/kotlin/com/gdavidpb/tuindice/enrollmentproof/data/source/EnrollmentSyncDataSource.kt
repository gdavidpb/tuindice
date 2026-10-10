package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncReport
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
	// would go on announcing what is no longer true. Two steps, in this order:
	// 1. The saved report stops denying the enrollment right away, so the notice is gone before the
	//    user is back on the record, without waiting for the network.
	// 2. A sync is asked for past the cooldown, to bring whatever else changed with the enrollment.
	// Best effort both: the proof is already in hand, and a regular sync corrects it anyway.
	override suspend fun refreshWhenContradicted() {
		val report = lastSyncReport() ?: return
		val enrollment = report.sources.enrollment
		val deniedTheEnrollment = enrollment.situation != null ||
			enrollment.status == SyncSourceStatus.NotEnrolled

		if (!deniedTheEnrollment) return

		bestEffort {
			syncStatusRepository.setSyncReport(
				report.copy(
					sources = report.sources.copy(
						enrollment = SyncSourceReport(status = SyncSourceStatus.Success)
					)
				)
			)
		}

		bestEffort {
			syncRepository.scheduleSync(
				password = credentialsRepository.getPassword(),
				policy = SyncPolicy.ForceRefresh
			)
		}
	}

	private suspend fun lastSyncEnrollment(): SyncSourceReport? {
		return lastSyncReport()?.sources?.enrollment
	}

	private suspend fun lastSyncReport(): SyncReport? {
		return bestEffort { syncStatusRepository.getSyncReport() }
	}

	private suspend fun <T> bestEffort(block: suspend () -> T): T? {
		return runCatching { block() }
			.onFailure { throwable -> if (throwable is CancellationException) throw throwable }
			.getOrNull()
	}
}
