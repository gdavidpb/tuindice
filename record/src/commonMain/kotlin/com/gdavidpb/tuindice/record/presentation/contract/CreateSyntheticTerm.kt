package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import kotlinx.coroutines.flow.StateFlow
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.top_bar_create_synthetic_term
import tuindice.record.generated.resources.top_bar_edit_synthetic_term

object CreateSyntheticTerm {
	data class State(
		val editingTermId: String? = null,
		val editingTermKey: String? = null,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = false,
		val query: String = "",
		val periodOptions: List<SyntheticTermPeriodOption> = emptyList(),
		val selectedPeriod: SyntheticTermPeriodOption? = null,
		val selectedSubjects: List<SyntheticTermSubject> = emptyList(),
		val suggestedSubjects: List<SyntheticTermSubject> = emptyList(),
		val searchResults: List<SyntheticTermSubject> = emptyList(),
		val loadPreview: SyntheticTermLoadPreview? = null,
		val isLoadingLoadPreview: Boolean = false,
		val hasLoadPreviewError: Boolean = false,
		val isRefreshingSearch: Boolean = false,
		val hasSearchError: Boolean = false,
		val isSubmitting: Boolean = false
	) : ViewState() {
		val canSubmit: Boolean
			get() = selectedPeriod != null && selectedSubjects.isNotEmpty() && !isSubmitting

		override val topBarTitle: UiText
			get() = if (editingTermId == null) {
				UiText.Resource(Res.string.top_bar_create_synthetic_term)
			} else {
				UiText.Resource(Res.string.top_bar_edit_synthetic_term)
			}

		val isEditing: Boolean
			get() = editingTermId != null
	}

	sealed class Action : ViewAction() {
		data class Observe(
			val queryFlow: StateFlow<String>,
			val selectedSubjectsFlow: StateFlow<List<SyntheticTermSubject>>,
			val selectedPeriodKeyFlow: StateFlow<String?>,
			val editingTermIdFlow: StateFlow<String?>,
			val editingTermKeyFlow: StateFlow<String?>
		) : Action()

		data class UpdateQuery(val query: String) : Action()
		data class CreateTerm(
			val editingTermId: String?,
			val editingTermKey: String?,
			val period: SyntheticTermPeriodOption,
			val subjects: List<SyntheticTermSubject>
		) : Action()
	}

	sealed class Effect : ViewEffect() {
		data object NavigateBack : Effect()
		data class ShowSnackBar(val message: String) : Effect()
	}
}
