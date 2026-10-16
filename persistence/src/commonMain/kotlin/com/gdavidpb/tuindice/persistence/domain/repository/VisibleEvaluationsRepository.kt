package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * The evaluations the Evaluations tab lists: the confirmed ones plus the pending ones of the
 * outbox, as the tab sees them, left only with those whose attempt is one of the numerically graded
 * attempts of the current term (the same filter the tab applies before drawing its list).
 * [ObservedSyncedSnapshot.hasSynced] tells "no evaluations" (`true` with an empty list) apart from
 * "not downloaded yet" (`false`). It reads the local database only: it never triggers a download.
 *
 * It emits again when the evaluations change or when the current term does.
 *
 * Warning for whoever consumes it: the `state` of each [Evaluation] is computed when it is mapped
 * and does not advance with the clock. Whoever needs the state "as of today" derives it with
 * `EvaluationStateEngine.stateOf(...)` and its own "today".
 */
interface VisibleEvaluationsRepository {
	fun observeVisibleEvaluationsFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>>
}
