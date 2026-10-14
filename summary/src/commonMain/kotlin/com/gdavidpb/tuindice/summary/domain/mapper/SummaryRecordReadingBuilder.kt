package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.RecordStatisticsEngine
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordReading
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordState

// The record is projected here and only here. The finished term and the closed-term statistics are both read from
// this one projection, so the two never disagree about the record.
internal fun recordReadingOf(recordState: SummaryRecordState): SummaryRecordReading {
	return when (recordState) {
		is SummaryRecordState.Ready -> {
			val projection = RecordProjectionEngine.projectAcademic(recordState.record)

			SummaryRecordReading.Ready(
				record = recordState.record,
				projection = projection,
				closed = RecordStatisticsEngine.statisticsOf(projection)
			)
		}

		SummaryRecordState.NewStudentWithoutRecord -> SummaryRecordReading.NewStudentWithoutRecord

		SummaryRecordState.NotReady -> SummaryRecordReading.NotReady
	}
}
