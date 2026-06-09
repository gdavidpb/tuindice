package com.gdavidpb.tuindice.subjects.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.base.domain.model.GradingMode

data class SubjectSearchResult(
	val subjectCode: String,
	val name: String,
	val credits: Int,
	val gradingMode: GradingMode?,
	val pensumStatus: AcademicPensumNodeStatus? = null
)
