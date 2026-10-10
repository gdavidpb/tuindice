package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord

/** What the queue of record edits needs to do when a newer confirmed record lands from outside it. */
interface AcademicRecordOutboxDataRepository {
	/**
	 * Called after [record] was stored by a sync. The edits still waiting move to its revision, and
	 * the ones aimed at a subject the record no longer has are dropped without a word: there is
	 * nothing left for them to change, and the server would only answer 404.
	 */
	suspend fun reconcileWithConfirmedRecord(record: VersionedAcademicRecord)
}
