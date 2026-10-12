package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * The evaluations exactly as the Evaluations tab sees them: the confirmed ones plus the pending
 * ones of the outbox. [ObservedSyncedSnapshot.hasSynced] tells "no evaluations" (`true` with an
 * empty list) apart from "not downloaded yet" (`false`). It reads the local database only: it never
 * triggers a download.
 *
 * Two warnings for whoever consumes it:
 * 1. The `state` of each [Evaluation] is computed when it is mapped and does not advance with the
 *    clock. Whoever needs the state "as of today" derives it with `EvaluationStateEngine.stateOf(...)`
 *    and its own "today".
 * 2. It may carry evaluations whose attempt is no longer in the visible academic record (see
 *    [VisibleAcademicRecordRepository]): the consumer filters them against the visible record.
 */
interface VisibleEvaluationsRepository {
	fun observeVisibleEvaluationsFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>>
}
