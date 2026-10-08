package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
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

			if (action.query.trim().length < MinimumSearchQueryLength) {
				state.copy(
					query = action.query,
					searchResults = emptyList(),
					isRefreshingSearch = false,
					hasSearchError = false
				)
			} else {
				state.copy(
					query = action.query,
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
			// A snapshot already queued can answer a query that is no longer the typed one: the rest of it is
			// still the truth, its search results are not.
			val resultsAreOfTheTypedQuery = state.isAbout(event.searchQuery)
			val updatedState = state.copy(
				editingTermId = event.editingTermId,
				editingTermKey = event.editingTermKey,
				periodOptions = event.periodOptions,
				selectedPeriod = event.selectedPeriod,
				selectedSubjects = event.selectedSubjects,
				suggestedSubjects = event.suggestedSubjects,
				searchResults = if (resultsAreOfTheTypedQuery) event.searchResults else state.searchResults,
				submitError = UiText.Empty,
				hasSearchError = if (resultsAreOfTheTypedQuery && event.searchResults.isNotEmpty()) {
					false
				} else {
					state.hasSearchError
				}
			)

			updatedState.withInitialDraft(event = event)
		}

		on<CreateSyntheticTermInternalEvent.SearchCleared> { state, event ->
			if (!state.isAbout(event.query)) {
				state
			} else {
				state.copy(
					searchResults = emptyList(),
					isRefreshingSearch = false,
					hasSearchError = false
				)
			}
		}

		on<CreateSyntheticTermInternalEvent.SearchStarted> { state, event ->
			if (!state.isAbout(event.query)) {
				state
			} else {
				state.copy(
					isRefreshingSearch = true,
					hasSearchError = false
				)
			}
		}

		on<CreateSyntheticTermInternalEvent.SearchSucceeded> { state, event ->
			if (!state.isAbout(event.query)) {
				state
			} else {
				state.copy(
					isRefreshingSearch = false,
					hasSearchError = false
				)
			}
		}

		on<CreateSyntheticTermInternalEvent.SearchFailed> { state, event ->
			if (!state.isAbout(event.query)) {
				state
			} else {
				state.copy(
					isRefreshingSearch = false,
					hasSearchError = state.searchResults.isEmpty()
				)
			}
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

// The draft the student has to move away from for leaving to be worth a warning. Editing: the loaded term, once its
// subjects have arrived. Creating: the form opens with a period already chosen and no subjects.
private fun CreateSyntheticTerm.State.withInitialDraft(
	event: CreateSyntheticTermInternalEvent.SnapshotObserved
): CreateSyntheticTerm.State {
	val period = event.selectedPeriod

	return when {
		initialDraft != null -> this
		event.editingTermId != null -> if (event.selectedSubjects.isEmpty()) this else copy(initialDraft = draft)
		period != null -> copy(
			initialDraft = CreateSyntheticTerm.Draft(
				periodKey = period.termKey,
				subjectCodes = emptyList()
			)
		)
		else -> this
	}
}

private fun CreateSyntheticTerm.State.isAbout(query: String) =
	SubjectCatalogSearchNormalizer.normalize(this.query) == SubjectCatalogSearchNormalizer.normalize(query)

private const val MinimumSearchQueryLength = 2
