package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// The same snapshot the Evaluations tab observes; see VisibleEvaluationsRepository for what the
// consumer has to do with it.
class VisibleEvaluationsDataSource(
	private val evaluationRepository: EvaluationRepository
) : VisibleEvaluationsRepository {
	override fun observeVisibleEvaluationsFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		return flow { emitAll(evaluationRepository.observeEvaluationsSnapshotFlow()) }
	}
}
