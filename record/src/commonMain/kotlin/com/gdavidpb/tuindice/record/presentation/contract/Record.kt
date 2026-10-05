package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.applying
import com.gdavidpb.tuindice.record.presentation.model.RecordFailedArt
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.top_bar_record

object Record {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_record),
		override val topBarConfig: TopBarConfig = TopBarConfig.Record,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState {
		data object Idle : State()

		data object Loading : State()

		data class Content(
			val viewMode: RecordViewMode,
			val record: AcademicRecord,
			val selectedTermId: String,
			val inFlightSelection: InFlightSelection? = null,
			val notice: RecordNotice? = null,
			// Whether the selected term is the current one with something scheduled: what puts the
			// schedule icon on the bar. Resolved with the observation, so nothing asks it while drawing.
			val hasSelectedTermSchedule: Boolean = false
		) : State() {
			/**
			 * Lo que la pantalla proyecta: el expediente observado más el override del
			 * gesto en curso. Mientras el usuario arrastra no hay escritura en datos,
			 * así que este es el único sitio donde ese valor existe.
			 */
			val visibleRecord: AcademicRecord
				get() = inFlightSelection?.let { selection -> record.applying(selection.override) }
					?: record
		}

		data class InFlightSelection(
			val override: AttemptOverride,
			val isCommitted: Boolean
		)

		// Nothing to list. What it says arrives resolved: the generic copy, or the final annulment
		// that is the reason there is nothing.
		data class Empty(
			val title: UiText,
			val message: UiText
		) : State()

		// The record could not be loaded. What it says and the art above it arrive resolved, because
		// a university with no record for this account yet is not a failure of ours.
		data class Failed(
			val title: UiText,
			val message: UiText,
			val art: RecordFailedArt
		) : State()
	}

	sealed class Action : ViewAction {
		data object ObserveRecord : Action()
		data object EnsureRecordLoaded : Action()
		data object RefreshRecord : Action()
		class SetViewMode(val viewMode: RecordViewMode) : Action()
		class SelectTerm(
			val termId: String
		) : Action()

		class UpsertAttemptSelection(
			val attemptId: String,
			val grade: Int? = null,
			val outcome: AttemptOutcome? = null,
			val commit: Boolean
		) : Action()

		class DeleteSyntheticTerm(val termId: String) : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateToOutdatedCredentials : Effect()
		class ShowSnackBar(val message: String) : Effect()
		class ShowTopBarBanner(
			val viewMode: RecordViewMode,
			val behavior: TopBarBannerBehavior
		) : Effect()
	}
}
