package com.gdavidpb.tuindice.subjects.presentation.mapper

import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem

fun SubjectSearchResult.toSubjectSearchResultItem(): SubjectSearchResultItem {
	return SubjectSearchResultItem(
		subjectCode = subjectCode,
		name = name,
		creditsText = "$credits UC"
	)
}
