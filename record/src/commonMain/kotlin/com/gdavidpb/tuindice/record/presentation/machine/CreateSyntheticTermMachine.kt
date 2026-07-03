package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermEditSeedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermPreviewUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermCreationUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RefreshSyntheticTermSubjectSearchUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import com.gdavidpb.tuindice.record.domain.usecase.param.LoadSyntheticTermPreviewParams
import com.gdavidpb.tuindice.record.domain.usecase.param.ObserveSyntheticTermCreationParams
import com.gdavidpb.tuindice.record.domain.usecase.param.RefreshSyntheticTermSubjectSearchParams
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedTermParams
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.mapper.toCreateTermSubjectItem
import com.gdavidpb.tuindice.record.presentation.mapper.toSubmitErrorText
import com.gdavidpb.tuindice.record.presentation.transition.createSyntheticTermTransitions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlin.time.Duration.Companion.milliseconds
import tuindice.record.generated.resources.Res

@OptIn(ExperimentalCoroutinesApi::class)
class CreateSyntheticTermMachine(
	internal val draft: CreateSyntheticTermDraft,
	private val observeSyntheticTermCreationUseCase: ObserveSyntheticTermCreationUseCase,
	private val refreshSyntheticTermSubjectSearchUseCase: RefreshSyntheticTermSubjectSearchUseCase,
	private val loadSyntheticTermPreviewUseCase: LoadSyntheticTermPreviewUseCase,
	private val loadSyntheticTermEditSeedUseCase: LoadSyntheticTermEditSeedUseCase,
	private val createSyntheticTermUseCase: CreateSyntheticTermUseCase,
	private val updateSyntheticTermUseCase: UpdateSyntheticTermUseCase,
	private val setSelectedTermUseCase: SetSelectedTermUseCase
) : ScreenMachine<CreateSyntheticTerm.State, CreateSyntheticTerm.Effect> {
	override fun initialState(): CreateSyntheticTerm.State = CreateSyntheticTerm.State()

	override fun define(
		host: MachineHost<CreateSyntheticTerm.Effect>
	): MachineDefinition<CreateSyntheticTerm.State> {
		return MachineDefinition.define {
			createSyntheticTermTransitions(machine = this@CreateSyntheticTermMachine, host = host)
		}
	}

	internal fun startObservation(host: MachineHost<CreateSyntheticTerm.Effect>) {
		host.launchMachineJob {
			merge(
				observeSnapshot(),
				searchPipeline(),
				loadPreviewPipeline()
			).collect { event -> host.processInternalEvent(event) }
		}
	}

	internal fun configureTerm(
		host: MachineHost<CreateSyntheticTerm.Effect>,
		termId: String?
	) {
		if (!draft.tryConfigure(termId)) return
		if (termId == null) {
			draft.clearEditing()
			return
		}

		host.launchMachineJob {
			loadSyntheticTermEditSeedUseCase.execute(termId).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					draft.applySeed(useCaseState.value)
				}
			}
		}
	}

	internal fun submit(
		host: MachineHost<CreateSyntheticTerm.Effect>,
		state: CreateSyntheticTerm.State
	) {
		// EFSM guard over extended state: mirrors the submit button's enablement and
		// also ignores re-submission while a submit is in flight.
		if (!state.canSubmit) return

		val editingTermId = state.editingTermId
		val editingTermKey = state.editingTermKey
		val period = requireNotNull(state.selectedPeriod)
		val subjects = state.selectedSubjects.map { item -> item.subject }

		host.launchMachineJob {
			val params = CreateSyntheticTermParams(
				editingTermId = editingTermId,
				editingTermKey = editingTermKey,
				period = period,
				subjects = subjects
			)
			val result = if (editingTermId == null) {
				createSyntheticTermUseCase.execute(params)
			} else {
				updateSyntheticTermUseCase.execute(params)
			}

			result.collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						CreateSyntheticTermInternalEvent.SubmitStarted
					)

					is UseCaseState.Data -> {
						setSelectedTermUseCase.execute(
							SetSelectedTermParams(
								viewMode = RecordViewMode.Projection,
								termId = useCaseState.value
							)
						).collect()

						host.processInternalEvent(
							CreateSyntheticTermInternalEvent.SubmitSucceeded
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(
						CreateSyntheticTermInternalEvent.SubmitFailed(
							error = useCaseState.error.toSubmitErrorText()
						)
					)
				}
			}
		}
	}

	private fun observeSnapshotParams(): ObserveSyntheticTermCreationParams {
		return ObserveSyntheticTermCreationParams(
			queryFlow = draft.queryFlow,
			selectedSubjectsFlow = draft.selectedSubjectsFlow,
			selectedPeriodKeyFlow = draft.selectedPeriodKeyFlow,
			editingTermIdFlow = draft.editingTermIdFlow,
			editingTermKeyFlow = draft.editingTermKeyFlow
		)
	}

	private fun observeSnapshot(): Flow<CreateSyntheticTermInternalEvent> {
		return observeSyntheticTermCreationUseCase.execute(
			observeSnapshotParams()
		).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Data -> {
					val snapshot = useCaseState.value

					CreateSyntheticTermInternalEvent.SnapshotObserved(
						editingTermId = snapshot.editingTermId,
						editingTermKey = snapshot.editingTermKey,
						periodOptions = snapshot.periodOptions,
						selectedPeriod = snapshot.selectedPeriod,
						selectedSubjects = snapshot.selectedSubjects.map { subject ->
							subject.toCreateTermSubjectItem()
						},
						suggestedSubjects = snapshot.suggestedSubjects.map { subject ->
							subject.toCreateTermSubjectItem()
						},
						searchResults = snapshot.searchResults.map { subject ->
							subject.toCreateTermSubjectItem()
						}
					)
				}

				is UseCaseState.Loading,
				is UseCaseState.Error,
				-> null
			}
		}
	}

	private fun searchPipeline(): Flow<CreateSyntheticTermInternalEvent> {
		return draft.queryFlow
			.distinctUntilChanged { old, new ->
				SubjectCatalogSearchNormalizer.normalize(old) ==
					SubjectCatalogSearchNormalizer.normalize(new)
			}
			.flatMapLatest { query ->
				val normalizedQuery = SubjectCatalogSearchNormalizer.normalize(query)

				if (normalizedQuery.length < MinimumSearchQueryLength) {
					flowOf<CreateSyntheticTermInternalEvent>(
						CreateSyntheticTermInternalEvent.SearchCleared
					)
				} else {
					flow {
						delay(SearchDebounceMillis.milliseconds)

						emit(CreateSyntheticTermInternalEvent.SearchStarted)

						refreshSyntheticTermSubjectSearchUseCase.execute(
							RefreshSyntheticTermSubjectSearchParams(query = query)
						).collect { useCaseState ->
							when (useCaseState) {
								is UseCaseState.Data ->
									emit(CreateSyntheticTermInternalEvent.SearchSucceeded)

								is UseCaseState.Error ->
									emit(CreateSyntheticTermInternalEvent.SearchFailed)

								is UseCaseState.Loading -> Unit
							}
						}
					}
				}
			}
	}

	private fun loadPreviewPipeline(): Flow<CreateSyntheticTermInternalEvent> {
		return observeSyntheticTermCreationUseCase.execute(
			observeSnapshotParams()
		)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> {
						val snapshot = useCaseState.value

						LoadPreviewSelection(
							termKey = snapshot.selectedPeriod?.termKey,
							subjects = snapshot.selectedSubjects,
							subjectCodes = snapshot.selectedSubjects.map { subject ->
								subject.subjectCode
							}
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
					flowOf<CreateSyntheticTermInternalEvent>(
						CreateSyntheticTermInternalEvent.LoadPreviewCleared
					)
				} else {
					flow {
						emit(CreateSyntheticTermInternalEvent.LoadPreviewStarted)

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
										CreateSyntheticTermInternalEvent.LoadPreviewLoaded(
											preview = useCaseState.value
										)
									)

								is UseCaseState.Error ->
									emit(CreateSyntheticTermInternalEvent.LoadPreviewFailed)

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
