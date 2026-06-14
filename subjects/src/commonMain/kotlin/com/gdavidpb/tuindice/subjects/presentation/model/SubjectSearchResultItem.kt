package com.gdavidpb.tuindice.subjects.presentation.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus

data class SubjectSearchResultItem(
	val subjectCode: String,
	val name: String,
	val creditsText: String,
	val pensumStatus: AcademicPensumNodeStatus? = null
)
