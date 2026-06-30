package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.top_bar_create_synthetic_term
import tuindice.record.generated.resources.top_bar_edit_synthetic_term

object CreateSyntheticTerm {
	data class State(
		val editingTermId: String? = null,
		val editingTermKey: String? = null,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = false,
		val initialDraft: Draft? = null,
		val query: String = "",
		val querySelectionStart: Int = 0,
		val querySelectionEnd: Int = 0,
		val selectedAddSubjectTab: CreateTermAddSubjectTab = CreateTermAddSubjectTab.Suggested,
		val periodOptions: List<SyntheticTermPeriodOption> = emptyList(),
		val selectedPeriod: SyntheticTermPeriodOption? = null,
		val selectedSubjects: List<CreateTermSubjectItem> = emptyList(),
		val suggestedSubjects: List<CreateTermSubjectItem> = emptyList(),
		val searchResults: List<CreateTermSubjectItem> = emptyList(),
		val loadPreview: SyntheticTermLoadPreview? = null,
		val isLoadingLoadPreview: Boolean = false,
		val hasLoadPreviewError: Boolean = false,
		val isRefreshingSearch: Boolean = false,
		val hasSearchError: Boolean = false,
		val isSubmitting: Boolean = false,
		val submitError: UiText = UiText.Empty
	) : ViewState {
		val draft: Draft
			get() = Draft(
				periodKey = selectedPeriod?.termKey,
				subjectCodes = selectedSubjects.map { subject -> subject.subjectCode }
			)

		val hasDraftChanges: Boolean
			get() = initialDraft?.let { draft -> draft != this.draft } ?: !isEditing

		val canSubmit: Boolean
			get() = selectedPeriod != null &&
				selectedSubjects.isNotEmpty() &&
				!isSubmitting &&
				hasDraftChanges

		override val topBarTitle: UiText
			get() = if (editingTermId == null) {
				UiText.Resource(Res.string.top_bar_create_synthetic_term)
			} else {
				UiText.Resource(Res.string.top_bar_edit_synthetic_term)
			}

		val isEditing: Boolean
			get() = editingTermId != null
	}

	data class Draft(
		val periodKey: String?,
		val subjectCodes: List<String>
	)

	sealed class Action : ViewAction {
		data object Observe : Action()

		data class ConfigureTerm(
			val termId: String?
		) : Action()

		data class UpdateQuery(
			val query: String,
			val selectionStart: Int,
			val selectionEnd: Int
		) : Action()

		data class SelectAddSubjectTab(
			val tab: CreateTermAddSubjectTab
		) : Action()

		data class SelectPeriod(
			val termKey: String
		) : Action()

		data class AddSubject(
			val subjectItem: CreateTermSubjectItem
		) : Action()

		data class RemoveSubject(
			val subjectCode: String
		) : Action()

		data object CreateTerm : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateBack : Effect()
	}
}
