package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.RecordStatistics

/**
 * What the hub reads of the record, read once for each emission of the record. A ready record comes with the
 * projection it is read through and the statistics of its closed terms, which do not depend on the moment they are
 * asked for. The other states carry nothing beyond themselves.
 */
internal sealed interface SummaryRecordReading {
	data class Ready(
		val record: AcademicRecord,
		val projection: RecordProjection,
		val closed: RecordStatistics
	) : SummaryRecordReading

	data object NewStudentWithoutRecord : SummaryRecordReading

	data object NotReady : SummaryRecordReading
}
