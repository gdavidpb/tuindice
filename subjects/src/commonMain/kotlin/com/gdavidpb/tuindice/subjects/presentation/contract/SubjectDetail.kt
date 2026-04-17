package com.gdavidpb.tuindice.subjects.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab

object SubjectDetail {
	sealed class State(
		override val topBarTitle: String = "Sobre esta materia",
		override val isTopBarVisible: Boolean = true
	) : ViewState() {
		data object Loading : State()

		data class Content(
			val detail: SubjectDetailModel,
			val selectedTab: SubjectSegmentTab
		) : State()

		data class Unavailable(
			val subjectCode: String
		) : State()

		data class Failed(
			val subjectCode: String
		) : State()
	}

	sealed class Action : ViewAction() {
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

	sealed class Effect : ViewEffect()
}
