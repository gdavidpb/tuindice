package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordOutboxDataRepository

object NoOpRecordOutboxDataRepository : AcademicRecordOutboxDataRepository {
	override suspend fun reconcileWithConfirmedRecord(record: VersionedAcademicRecord) = Unit
}
