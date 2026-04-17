package com.gdavidpb.tuindice.subjects.presentation.mapper

import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail

internal fun SubjectDetailResult.toViewState(): SubjectDetail.State {
	return when (this) {
		is SubjectDetailResult.Ready ->
			SubjectDetail.State.Content(
				detail = detail,
				selectedTab = if (detail.careerSegment != null) {
					SubjectSegmentTab.CAREER
				} else {
					SubjectSegmentTab.GLOBAL
				}
			)

		is SubjectDetailResult.Unavailable ->
			SubjectDetail.State.Unavailable(subjectCode = subjectCode)
	}
}
