package com.gdavidpb.tuindice.subjects.presentation.model

import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchPensumStatus

data class SubjectSearchResultItem(
	val subjectCode: String,
	val name: String,
	val creditsText: String,
	val pensumStatus: SubjectSearchPensumStatus? = null
)
