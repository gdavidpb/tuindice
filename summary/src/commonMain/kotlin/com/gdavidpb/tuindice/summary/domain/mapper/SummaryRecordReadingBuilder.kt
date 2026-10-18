package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.RecordStatisticsEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
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

// What the summary reads of the record: a ready one has the week and the term. A new student the university has no record
// for yet has no term yet, which is not an error; any other state of the record leaves only the career.
internal fun summaryRecordStateOf(record: AcademicRecord?, prerequisite: RecordDataPrerequisiteState): SummaryRecordState {
	return when {
		record != null && prerequisite.isReady -> SummaryRecordState.Ready(record)
		prerequisite.isNewStudentWithoutRecord -> SummaryRecordState.NewStudentWithoutRecord
		else -> SummaryRecordState.NotReady
	}
}
