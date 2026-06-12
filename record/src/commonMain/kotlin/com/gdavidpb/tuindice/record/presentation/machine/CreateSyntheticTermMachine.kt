package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
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
import com.gdavidpb.tuindice.record.presentation.transition.createSyntheticTermTransitions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlin.time.Duration.Companion.milliseconds
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_error_duplicate_subject
import tuindice.record.generated.resources.create_term_error_generic
import tuindice.record.generated.resources.create_term_error_period_in_past
import tuindice.record.generated.resources.create_term_error_record_unavailable
import tuindice.record.generated.resources.create_term_error_subject_already_planned
import tuindice.record.generated.resources.create_term_error_subject_already_taken
import tuindice.record.generated.resources.create_term_error_term_already_exists
import tuindice.record.generated.resources.create_term_error_term_must_be_after_latest

@OptIn(ExperimentalCoroutinesApi::class)
class CreateSyntheticTermMachine(
	private val observeSyntheticTermCreationUseCase: ObserveSyntheticTermCreationUseCase,
	private val refreshSyntheticTermSubjectSearchUseCase: RefreshSyntheticTermSubjectSearchUseCase,
	private val loadSyntheticTermPreviewUseCase: LoadSyntheticTermPreviewUseCase,
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

	internal fun startObservation(
		host: MachineHost<CreateSyntheticTerm.Effect>,
		action: CreateSyntheticTerm.Action.Observe
	) {
		host.launchMachineJob {
			merge(
				observeSelectedTab(action),
				observeSnapshot(action),
				searchPipeline(action),
				loadPreviewPipeline(action)
			).collect { event -> host.processInternalEvent(event) }
		}
	}

	internal fun submit(
		host: MachineHost<CreateSyntheticTerm.Effect>,
		action: CreateSyntheticTerm.Action.CreateTerm
	) {
		host.launchMachineJob {
			val params = CreateSyntheticTermParams(
				editingTermId = action.editingTermId,
				editingTermKey = action.editingTermKey,
				period = action.period,
				subjects = action.subjects
			)
			val result = if (action.editingTermId == null) {
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

	private fun observeSelectedTab(
		action: CreateSyntheticTerm.Action.Observe
	): Flow<CreateSyntheticTermInternalEvent> {
		return action.selectedAddSubjectTabFlow.map { tab ->
			CreateSyntheticTermInternalEvent.TabSelected(tab = tab)
		}
	}

	private fun observeSnapshot(
		action: CreateSyntheticTerm.Action.Observe
	): Flow<CreateSyntheticTermInternalEvent> {
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

	private fun searchPipeline(
		action: CreateSyntheticTerm.Action.Observe
	): Flow<CreateSyntheticTermInternalEvent> {
		return action.queryFlow
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

	private fun loadPreviewPipeline(
		action: CreateSyntheticTerm.Action.Observe
	): Flow<CreateSyntheticTermInternalEvent> {
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

private fun RecordUseCaseError?.toSubmitErrorText(): UiText {
	return when (this) {
		is RecordUseCaseError.SyntheticTermValidation -> UiText.Resource(
			when (reason) {
				SyntheticTermValidationError.RECORD_UNAVAILABLE,
				SyntheticTermValidationError.TERM_NOT_FOUND,
				-> Res.string.create_term_error_record_unavailable

				SyntheticTermValidationError.PERIOD_IN_PAST -> Res.string.create_term_error_period_in_past
				SyntheticTermValidationError.TERM_ALREADY_EXISTS -> Res.string.create_term_error_term_already_exists
				SyntheticTermValidationError.TERM_MUST_BE_AFTER_LATEST -> Res.string.create_term_error_term_must_be_after_latest
				SyntheticTermValidationError.DUPLICATE_SUBJECT -> Res.string.create_term_error_duplicate_subject
				SyntheticTermValidationError.SUBJECT_ALREADY_TAKEN -> Res.string.create_term_error_subject_already_taken
				SyntheticTermValidationError.SUBJECT_ALREADY_PLANNED -> Res.string.create_term_error_subject_already_planned
			}
		)

		RecordUseCaseError.NoConnection,
		RecordUseCaseError.Timeout,
		RecordUseCaseError.Unauthorized,
		RecordUseCaseError.Unavailable,
		null,
		-> UiText.Resource(Res.string.create_term_error_generic)
	}
}

private const val MinimumSearchQueryLength = 2
private const val SearchDebounceMillis = 300L
private const val LoadPreviewDebounceMillis = 250L
