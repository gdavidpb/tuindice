package com.gdavidpb.tuindice.subjects.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.top_bar_subject_detail

object SubjectDetail {
	sealed class State(
		override val topBarTitle: UiText,
		override val isTopBarVisible: Boolean = true
	) : ViewState {
		data object Idle : State(topBarTitle = UiText.Empty)

		data object Loading : State(topBarTitle = UiText.Empty)

		data class Content(
			val detail: SubjectDetailItem
		) : State(topBarTitle = detail.id.toSubjectDetailTopBarTitle())

		data class Unavailable(
			val subjectCode: String
		) : State(topBarTitle = subjectCode.toSubjectDetailTopBarTitle())

		data class Failed(
			val subjectCode: String
		) : State(topBarTitle = subjectCode.toSubjectDetailTopBarTitle())
	}

	sealed class Action : ViewAction {
		data class LoadSubjectDetail(
			val subjectCode: String
		) : Action()

		data class RefreshSubjectDetail(
			val subjectCode: String
		) : Action()

		data class SelectSubjectSegmentTab(
			val tab: SubjectSegmentTab
		) : Action()
	}

	sealed class Effect : ViewEffect
}

private fun String.toSubjectDetailTopBarTitle(): UiText {
	return UiText.Resource(Res.string.top_bar_subject_detail, args = listOf(this))
}
