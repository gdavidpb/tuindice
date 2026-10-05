package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.RecordRejection
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * Surfaces server-side terminal rejections of record edits (terms and grades, told apart so each
 * gets its own words). Each emission is acknowledged
 * (the parked envelope is discarded) before it reaches presentation: the visible record
 * already reverted the rejected change, the user gets told exactly once, and the sign-out
 * flush can no longer re-send a mutation the server already refused for good.
 */
class ObserveSyntheticTermRejectionsUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, List<RecordRejection>, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<List<RecordRejection>> {
		return academicRecordRepository.observeTerminallyRejectedMutationsFlow()
			.mapNotNull { rejections ->
				if (rejections.isEmpty()) return@mapNotNull null

				academicRecordRepository.acknowledgeTerminallyRejectedMutations(
					rejections.map { rejection -> rejection.mutationId }
				)
				rejections
			}
	}
}
