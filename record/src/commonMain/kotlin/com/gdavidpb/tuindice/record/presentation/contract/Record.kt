package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

object Record {
	sealed class State(
		override val topBarTitle: String = "Informe Académico",
		override val topBarConfig: TopBarConfig = TopBarConfig.Record,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState() {
		data object Loading : State()

		data class Content(
			val viewMode: RecordViewMode,
			val record: AcademicRecord,
			val selectedTermId: String
		) : State()

		data object Empty : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction() {
		data object ObserveRecord : Action()
		data object RefreshRecord : Action()
		class SetViewMode(val viewMode: RecordViewMode) : Action()
		class SelectTerm(val termId: String) : Action()

		class UpsertAttemptSelection(
			val termId: String,
			val attemptId: String,
			val grade: Int? = null,
			val status: SubjectStatus? = null,
			val commit: Boolean
		) : Action()
	}

	sealed class Effect : ViewEffect() {
		data object NavigateToOutdatedCredentials : Effect()
		class ShowSnackBar(val message: String) : Effect()
	}
}
