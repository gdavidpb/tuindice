package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermInternalEvent
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermMachine

internal fun MachineDefinitionBuilder<CreateSyntheticTerm.State>.createSyntheticTermTransitions(
	machine: CreateSyntheticTermMachine,
	host: MachineHost<CreateSyntheticTerm.Effect>
) {
	from<CreateSyntheticTerm.State> {
		on<CreateSyntheticTerm.Action.Observe> { state, _ ->
			machine.startObservation(host = host)
			state
		}

		on<CreateSyntheticTerm.Action.ConfigureTerm> { state, action ->
			machine.configureTerm(host = host, termId = action.termId)
			if (action.termId == state.editingTermId) {
				state
			} else {
				state.copy(initialDraft = null)
			}
		}

		on<CreateSyntheticTerm.Action.UpdateQuery> { state, action ->
			machine.draft.setQuery(action.query)

			val selectionStart = action.selectionStart.coerceIn(0, action.query.length)
			val selectionEnd = action.selectionEnd.coerceIn(0, action.query.length)

			if (action.query.trim().length < MinimumSearchQueryLength) {
				state.copy(
					query = action.query,
					querySelectionStart = selectionStart,
					querySelectionEnd = selectionEnd,
					searchResults = emptyList(),
					isRefreshingSearch = false,
					hasSearchError = false
				)
			} else {
				state.copy(
					query = action.query,
					querySelectionStart = selectionStart,
					querySelectionEnd = selectionEnd,
					hasSearchError = false
				)
			}
		}

		on<CreateSyntheticTerm.Action.SelectAddSubjectTab> { state, action ->
			state.copy(selectedAddSubjectTab = action.tab)
		}

		on<CreateSyntheticTerm.Action.SelectPeriod> { state, action ->
			machine.draft.selectPeriod(termKey = action.termKey)
			state
		}

		on<CreateSyntheticTerm.Action.AddSubject> { state, action ->
			machine.draft.addSubject(subjectItem = action.subjectItem)
			state
		}

		on<CreateSyntheticTerm.Action.RemoveSubject> { state, action ->
			machine.draft.removeSubject(subjectCode = action.subjectCode)
			state
		}

		on<CreateSyntheticTerm.Action.CreateTerm> { state, _ ->
			machine.submit(host = host, state = state)
			state
		}

		on<CreateSyntheticTermInternalEvent.SnapshotObserved> { state, event ->
			val updatedState = state.copy(
				editingTermId = event.editingTermId,
				editingTermKey = event.editingTermKey,
				periodOptions = event.periodOptions,
				selectedPeriod = event.selectedPeriod,
				selectedSubjects = event.selectedSubjects,
				suggestedSubjects = event.suggestedSubjects,
				searchResults = event.searchResults,
				submitError = UiText.Empty,
				hasSearchError = if (event.searchResults.isNotEmpty()) false else state.hasSearchError
			)

			if (state.initialDraft == null && event.editingTermId != null) {
				updatedState.copy(initialDraft = updatedState.draft)
			} else {
				updatedState
			}
		}

		on<CreateSyntheticTermInternalEvent.SearchCleared> { state, _ ->
			state.copy(
				searchResults = emptyList(),
				isRefreshingSearch = false,
				hasSearchError = false
			)
		}

		on<CreateSyntheticTermInternalEvent.SearchStarted> { state, _ ->
			state.copy(
				isRefreshingSearch = true,
				hasSearchError = false
			)
		}

		on<CreateSyntheticTermInternalEvent.SearchSucceeded> { state, _ ->
			state.copy(
				isRefreshingSearch = false,
				hasSearchError = false
			)
		}

		on<CreateSyntheticTermInternalEvent.SearchFailed> { state, _ ->
			state.copy(
				isRefreshingSearch = false,
				hasSearchError = state.searchResults.isEmpty()
			)
		}

		on<CreateSyntheticTermInternalEvent.LoadPreviewCleared> { state, _ ->
			state.copy(
				loadPreview = null,
				isLoadingLoadPreview = false,
				hasLoadPreviewError = false
			)
		}

		on<CreateSyntheticTermInternalEvent.LoadPreviewStarted> { state, _ ->
			state.copy(
				loadPreview = null,
				isLoadingLoadPreview = true,
				hasLoadPreviewError = false
			)
		}

		on<CreateSyntheticTermInternalEvent.LoadPreviewLoaded> { state, event ->
			state.copy(
				loadPreview = event.preview,
				isLoadingLoadPreview = false,
				hasLoadPreviewError = false
			)
		}

		on<CreateSyntheticTermInternalEvent.LoadPreviewFailed> { state, _ ->
			state.copy(
				loadPreview = null,
				isLoadingLoadPreview = false,
				hasLoadPreviewError = true
			)
		}

		on<CreateSyntheticTermInternalEvent.SubmitStarted> { state, _ ->
			state.copy(
				isSubmitting = true,
				submitError = UiText.Empty
			)
		}

		on<CreateSyntheticTermInternalEvent.SubmitSucceeded>(
			emits = setOf(CreateSyntheticTerm.Effect.NavigateBack::class)
		) { state, _ ->
			host.sendEffect(CreateSyntheticTerm.Effect.NavigateBack)

			state.copy(
				isSubmitting = false,
				submitError = UiText.Empty
			)
		}

		on<CreateSyntheticTermInternalEvent.SubmitFailed> { state, event ->
			state.copy(
				isSubmitting = false,
				submitError = event.error
			)
		}
	}
}

private const val MinimumSearchQueryLength = 2
