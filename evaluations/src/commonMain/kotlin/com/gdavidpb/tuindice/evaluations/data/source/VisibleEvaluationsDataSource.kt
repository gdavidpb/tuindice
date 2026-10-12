package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.evaluations.domain.mapper.listedUnder
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// What the Evaluations tab lists: its own snapshot, filtered with the code the tab filters with
// (listedUnder, against the numeric attempts of the current term). The attempts come from the same
// observed display context the tab's use case reads, so a term change or a withdrawal reaches both. See
// VisibleEvaluationsRepository.
class VisibleEvaluationsDataSource(
	private val evaluationRepository: EvaluationRepository
) : VisibleEvaluationsRepository {
	override fun observeVisibleEvaluationsFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		return flow {
			emitAll(
				combine(
					evaluationRepository.observeEvaluationsSnapshotFlow(),
					evaluationRepository.observeDisplayContextFlow()
				) { snapshot, displayContext ->
					snapshot.copy(value = snapshot.value.listedUnder(displayContext.attempts))
				}
			)
		}
	}
}
