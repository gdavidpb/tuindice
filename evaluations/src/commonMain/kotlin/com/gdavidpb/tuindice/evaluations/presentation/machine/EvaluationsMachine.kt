package com.gdavidpb.tuindice.evaluations.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.model.SyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.model.resolveSyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.buildEvaluationsWeekItems
import com.gdavidpb.tuindice.evaluations.presentation.mapper.defaultEvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationsWeekGroupItemList
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toGradeSaveErrorMessage
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toRemoveErrorMessage
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toUpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.transition.evaluationsAnyStateTransitions
import com.gdavidpb.tuindice.evaluations.presentation.transition.evaluationsContentTransitions
import com.gdavidpb.tuindice.evaluations.presentation.transition.evaluationsEmptyTransitions
import com.gdavidpb.tuindice.evaluations.presentation.transition.evaluationsFailedTransitions
import com.gdavidpb.tuindice.evaluations.presentation.transition.evaluationsNoAttemptsTransitions
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluations_continuous_label
import tuindice.evaluations.generated.resources.evaluations_week_label
import tuindice.evaluations.generated.resources.snack_default_error
import tuindice.evaluations.generated.resources.snack_evaluation_removed
import tuindice.evaluations.generated.resources.snack_evaluation_set_grade

class EvaluationsMachine(
	private val getEvaluationsUseCase: GetEvaluationsUseCase,
	private val updateEvaluationsUseCase: UpdateEvaluationsUseCase,
	private val getEvaluationUseCase: GetEvaluationUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase,
	private val removeEvaluationUseCase: RemoveEvaluationUseCase
) : ScreenMachine<Evaluations.State, Evaluations.Effect> {
	override fun initialState(): Evaluations.State = Evaluations.State.Idle

	override fun define(host: MachineHost<Evaluations.Effect>): MachineDefinition<Evaluations.State> {
		return MachineDefinition.define {
			evaluationsContentTransitions(machine = this@EvaluationsMachine, host = host)
			evaluationsEmptyTransitions()
			evaluationsNoAttemptsTransitions()
			evaluationsFailedTransitions()
			evaluationsAnyStateTransitions(machine = this@EvaluationsMachine, host = host)
		}
	}

	internal fun startObservation(host: MachineHost<Evaluations.Effect>) {
		host.launchMachineJob {
			getEvaluationsUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> when (val evaluations = useCaseState.value) {
						GetEvaluations.WaitingForRecordData -> host.processInternalEvent(
							EvaluationsInternalEvent.EvaluationsWaitingObserved
						)

						GetEvaluations.RecordDataUnavailable -> host.processInternalEvent(
							EvaluationsInternalEvent.EvaluationsRecordDataUnavailableObserved
						)

						is GetEvaluations.NoAttempts -> host.processInternalEvent(
							EvaluationsInternalEvent.EvaluationsNoAttemptsObserved(
								reason = evaluations.reason
							)
						)

						is GetEvaluations.Content -> when (
							resolveSyncedContentResolution(
								hasContent = evaluations.evaluations.isNotEmpty(),
								hasSynced = evaluations.hasSyncedEvaluations,
								keepCurrentWhileWaiting = false
							)
						) {
							SyncedContentResolution.Content -> host.processInternalEvent(
								evaluations.toContentObservedEvent()
							)

							SyncedContentResolution.Empty -> host.processInternalEvent(
								EvaluationsInternalEvent.EvaluationsEmptyObserved
							)

							SyncedContentResolution.Loading,
							SyncedContentResolution.KeepCurrent,
							-> host.processInternalEvent(
								EvaluationsInternalEvent.EvaluationsWaitingObserved
							)
						}
					}

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationsInternalEvent.EvaluationsObservationFailed
					)
				}
			}
		}
	}

	internal fun refresh(host: MachineHost<Evaluations.Effect>) {
		host.launchMachineJob {
			updateEvaluationsUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						EvaluationsInternalEvent.EvaluationsRefreshStarted
					)

					is UseCaseState.Data -> Unit

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationsInternalEvent.EvaluationsRefreshFailed
					)
				}
			}
		}
	}

	internal fun loadGradePicker(
		host: MachineHost<Evaluations.Effect>,
		action: Evaluations.Action.ShowEvaluationGradeDialog
	) {
		host.launchMachineJob {
			getEvaluationUseCase.execute(action.evaluationId).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val evaluation = useCaseState.value

						if (evaluation != null) {
							host.processInternalEvent(
								EvaluationsInternalEvent.GradePickerLoaded(
									evaluationId = evaluation.id,
									evaluationName = action.evaluationName,
									subjectCode = action.subjectCode,
									grade = evaluation.grade ?: 0.0,
									maxGrade = evaluation.maxGrade
								)
							)
						} else {
							host.processInternalEvent(
								EvaluationsInternalEvent.GradePickerLoadFailed(
									message = getString(Res.string.snack_default_error)
								)
							)
						}
					}

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationsInternalEvent.GradePickerLoadFailed(
							message = getString(Res.string.snack_default_error)
						)
					)
				}
			}
		}
	}

	internal fun saveGrade(
		host: MachineHost<Evaluations.Effect>,
		action: Evaluations.Action.SetEvaluationGrade
	) {
		host.launchMachineJob {
			updateEvaluationUseCase.execute(action.toUpdateEvaluationParams()).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						EvaluationsInternalEvent.EvaluationGradeSaved(
							message = getString(Res.string.snack_evaluation_set_grade)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationsInternalEvent.EvaluationGradeSaveFailed(
							message = useCaseState.error.toGradeSaveErrorMessage()
						)
					)
				}
			}
		}
	}

	internal fun remove(host: MachineHost<Evaluations.Effect>, evaluationId: String) {
		host.launchMachineJob {
			removeEvaluationUseCase.execute(evaluationId).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						EvaluationsInternalEvent.EvaluationRemoved(
							message = getString(Res.string.snack_evaluation_removed)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationsInternalEvent.EvaluationRemoveFailed(
							message = useCaseState.error.toRemoveErrorMessage()
						)
					)
				}
			}
		}
	}

	private suspend fun GetEvaluations.Content.toContentObservedEvent(): EvaluationsInternalEvent {
		val mapping = getEvaluationItemMapping()
		val weekLabelPattern = getString(Res.string.evaluations_week_label)
		val continuousLabel = getString(Res.string.evaluations_continuous_label)
		val weekItems = buildEvaluationsWeekItems(
			currentTerm = displayContext.currentTerm,
			evaluations = evaluations,
			weekLabelPattern = weekLabelPattern,
			continuousLabel = continuousLabel
		)

		return EvaluationsInternalEvent.EvaluationsContentObserved(
			weekItems = weekItems,
			defaultWeekKey = defaultEvaluationsWeekKey(
				currentTerm = displayContext.currentTerm,
				evaluations = evaluations
			),
			evaluationWeekGroups = weekItems.toEvaluationsWeekGroupItemList(
				evaluations = evaluations,
				currentTerm = displayContext.currentTerm,
				attempts = displayContext.attempts,
				mapping = mapping
			)
		)
	}
}
