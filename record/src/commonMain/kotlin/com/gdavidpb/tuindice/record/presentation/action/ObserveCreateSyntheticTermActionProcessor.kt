package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermPreviewUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermCreationUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RefreshSyntheticTermSubjectSearchUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.LoadSyntheticTermPreviewParams
import com.gdavidpb.tuindice.record.domain.usecase.param.ObserveSyntheticTermCreationParams
import com.gdavidpb.tuindice.record.domain.usecase.param.RefreshSyntheticTermSubjectSearchParams
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveCreateSyntheticTermActionProcessor(
	private val observeSyntheticTermCreationUseCase: ObserveSyntheticTermCreationUseCase,
	private val refreshSyntheticTermSubjectSearchUseCase: RefreshSyntheticTermSubjectSearchUseCase,
	private val loadSyntheticTermPreviewUseCase: LoadSyntheticTermPreviewUseCase
) : ActionProcessor<
	CreateSyntheticTerm.State,
	CreateSyntheticTerm.Action.Observe,
	CreateSyntheticTerm.Effect
	>() {
	override suspend fun process(
		action: CreateSyntheticTerm.Action.Observe,
		sideEffect: (CreateSyntheticTerm.Effect) -> Unit
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return merge(
			observeSelectedAddSubjectTab(action),
			observeSnapshot(action),
			refreshSearch(action),
			observeLoadPreview(action)
		)
	}

	private fun observeSelectedAddSubjectTab(
		action: CreateSyntheticTerm.Action.Observe
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return action.selectedAddSubjectTabFlow
			.map { selectedAddSubjectTab ->
				suspend { state: CreateSyntheticTerm.State ->
					state.copy(selectedAddSubjectTab = selectedAddSubjectTab)
				}
			}
	}

	private fun observeSnapshot(
		action: CreateSyntheticTerm.Action.Observe
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return observeSyntheticTermCreationUseCase.execute(
			ObserveSyntheticTermCreationParams(
				queryFlow = action.queryFlow,
				selectedSubjectsFlow = action.selectedSubjectsFlow,
				selectedPeriodKeyFlow = action.selectedPeriodKeyFlow,
				editingTermIdFlow = action.editingTermIdFlow,
				editingTermKeyFlow = action.editingTermKeyFlow
			)
		).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Data ->
					suspend { state: CreateSyntheticTerm.State ->
						state.copy(
							editingTermId = useCaseState.value.editingTermId,
							editingTermKey = useCaseState.value.editingTermKey,
							periodOptions = useCaseState.value.periodOptions,
							selectedPeriod = useCaseState.value.selectedPeriod,
							selectedSubjects = useCaseState.value.selectedSubjects,
							suggestedSubjects = useCaseState.value.suggestedSubjects,
							searchResults = useCaseState.value.searchResults,
							submitError = UiText.Empty,
							hasSearchError = if (useCaseState.value.searchResults.isNotEmpty()) false else state.hasSearchError
						)
					}

				is UseCaseState.Loading,
				is UseCaseState.Error,
				-> null
			}
		}
	}

	private fun refreshSearch(
		action: CreateSyntheticTerm.Action.Observe
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return action.queryFlow
			.distinctUntilChanged { old, new ->
				SubjectCatalogSearchNormalizer.normalize(old) == SubjectCatalogSearchNormalizer.normalize(new)
			}
			.flatMapLatest { query ->
				val normalizedQuery = SubjectCatalogSearchNormalizer.normalize(query)
				if (normalizedQuery.length < MinimumSearchQueryLength) {
					flowOf(
						suspend { state: CreateSyntheticTerm.State ->
							state.copy(
								searchResults = emptyList(),
								isRefreshingSearch = false,
								hasSearchError = false
							)
						}
					)
				} else {
					flow {
						delay(SearchDebounceMillis.milliseconds)
						emit(
							suspend { state: CreateSyntheticTerm.State ->
								state.copy(
									isRefreshingSearch = true,
									hasSearchError = false
								)
							}
						)

						refreshSyntheticTermSubjectSearchUseCase.execute(
							RefreshSyntheticTermSubjectSearchParams(query = query)
						).collect { useCaseState ->
							when (useCaseState) {
								is UseCaseState.Data ->
									emit(
										suspend { state: CreateSyntheticTerm.State ->
											state.copy(
												isRefreshingSearch = false,
												hasSearchError = false
											)
										}
									)

								is UseCaseState.Error ->
									emit(
										suspend { state: CreateSyntheticTerm.State ->
											state.copy(
												isRefreshingSearch = false,
												hasSearchError = state.searchResults.isEmpty()
											)
										}
									)

								is UseCaseState.Loading -> Unit
							}
						}
					}
				}
			}
	}

	private fun observeLoadPreview(
		action: CreateSyntheticTerm.Action.Observe
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return observeSyntheticTermCreationUseCase.execute(
			ObserveSyntheticTermCreationParams(
				queryFlow = action.queryFlow,
				selectedSubjectsFlow = action.selectedSubjectsFlow,
				selectedPeriodKeyFlow = action.selectedPeriodKeyFlow,
				editingTermIdFlow = action.editingTermIdFlow,
				editingTermKeyFlow = action.editingTermKeyFlow
			)
		)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> {
						val snapshot = useCaseState.value
						LoadPreviewSelection(
							termKey = snapshot.selectedPeriod?.termKey,
							subjects = snapshot.selectedSubjects,
							subjectCodes = snapshot.selectedSubjects.map { subject -> subject.subjectCode }
						)
					}

					is UseCaseState.Loading,
					is UseCaseState.Error,
					-> null
				}
			}
			.distinctUntilChanged { old, new ->
				old.termKey == new.termKey && old.subjectCodes == new.subjectCodes
			}
			.flatMapLatest { selection ->
				val termKey = selection.termKey
				if (termKey == null || selection.subjects.isEmpty()) {
					flowOf(
						suspend { state: CreateSyntheticTerm.State ->
							state.copy(
								loadPreview = null,
								isLoadingLoadPreview = false,
								hasLoadPreviewError = false
							)
						}
					)
				} else {
					flow {
						emit(
							suspend { state: CreateSyntheticTerm.State ->
								state.copy(
									loadPreview = null,
									isLoadingLoadPreview = true,
									hasLoadPreviewError = false
								)
							}
						)
						delay(LoadPreviewDebounceMillis.milliseconds)
						loadSyntheticTermPreviewUseCase.execute(
							LoadSyntheticTermPreviewParams(
								termKey = termKey,
								subjectCodes = selection.subjectCodes
							)
						).collect { useCaseState ->
							when (useCaseState) {
								is UseCaseState.Data ->
									emit(
										suspend { state: CreateSyntheticTerm.State ->
											state.copy(
												loadPreview = useCaseState.value,
												isLoadingLoadPreview = false,
												hasLoadPreviewError = false
											)
										}
									)

								is UseCaseState.Error ->
									emit(
										suspend { state: CreateSyntheticTerm.State ->
											state.copy(
												loadPreview = null,
												isLoadingLoadPreview = false,
												hasLoadPreviewError = true
											)
										}
									)

								is UseCaseState.Loading -> Unit
							}
						}
					}
				}
			}
	}
}

private data class LoadPreviewSelection(
	val termKey: String?,
	val subjects: List<SyntheticTermSubject>,
	val subjectCodes: List<String>
)

private const val MinimumSearchQueryLength = 2
private const val SearchDebounceMillis = 300L
private const val LoadPreviewDebounceMillis = 250L
