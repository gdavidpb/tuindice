package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore

data class SyntheticTermCreationCommand(
	val termId: String,
	val periodYear: Int,
	val periodCode: AcademicTermPeriod,
	val attempts: List<SyntheticAttemptSeed>
) {
	data class SyntheticAttemptSeed(
		val attemptId: String,
		val subjectCode: String,
		val subjectName: String,
		val credits: Int,
		val gradingMode: AttemptGradingMode,
		val score: AttemptScore? = null,
		val outcome: AttemptOutcome? = null
	)
}
