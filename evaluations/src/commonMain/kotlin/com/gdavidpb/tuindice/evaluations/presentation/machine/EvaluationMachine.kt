package com.gdavidpb.tuindice.evaluations.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationGradeSectionItem
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationTypePickerItemList
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toAddEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toAddSubmitErrorMessage
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEditSubmitErrorMessage
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationAttemptPickerItems
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toGetEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toUpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.transition.evaluationAnyStateTransitions
import com.gdavidpb.tuindice.evaluations.presentation.transition.evaluationContentTransitions
import com.gdavidpb.tuindice.evaluations.presentation.utils.isDateInPast
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.snack_evaluation_added
import tuindice.evaluations.generated.resources.snack_evaluation_updated

class EvaluationMachine(
	private val getAvailableAttemptsUseCase: GetAvailableAttemptsUseCase,
	private val getEvaluationAndAvailableAttemptsUseCase: GetEvaluationAndAvailableAttemptsUseCase,
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase
) : ScreenMachine<Evaluation.State, Evaluation.Effect> {
	override fun initialState(): Evaluation.State = Evaluation.State.Loading

	override fun define(host: MachineHost<Evaluation.Effect>): MachineDefinition<Evaluation.State> {
		return MachineDefinition.define {
			evaluationContentTransitions(machine = this@EvaluationMachine, host = host)
			evaluationAnyStateTransitions(machine = this@EvaluationMachine, host = host)
		}
	}

	internal fun loadAvailableAttempts(host: MachineHost<Evaluation.Effect>) {
		host.launchMachineJob {
			getAvailableAttemptsUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						EvaluationInternalEvent.EditorLoadStarted
					)

					is UseCaseState.Data -> host.processInternalEvent(
						EvaluationInternalEvent.EditorContentLoaded(
							content = Evaluation.State.Content(
								attemptItems = useCaseState.value.toEvaluationAttemptPickerItems(
									selectedAttempt = null
								),
								typeItems = getEvaluationTypePickerItemList(
									selectedType = null
								),
								gradeSection = getEvaluationGradeSectionItem(
									isOverdue = false,
									grade = null,
									maxGrade = null
								)
							)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationInternalEvent.EditorLoadFailed
					)
				}
			}
		}
	}

	internal fun loadEvaluation(
		host: MachineHost<Evaluation.Effect>,
		action: Evaluation.Action.LoadEvaluation
	) {
		host.launchMachineJob {
			getEvaluationAndAvailableAttemptsUseCase.execute(
				action.toGetEvaluationParams()
			).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						EvaluationInternalEvent.EditorLoadStarted
					)

					is UseCaseState.Data -> with(useCaseState.value) {
						val selectedAttempt = availableAttempts.find { attempt ->
							attempt.id == evaluation?.attemptId
						}
						val isOverdue = evaluation?.let { loadedEvaluation ->
							loadedEvaluation.scheduleMode == EvaluationScheduleMode.DATED &&
								loadedEvaluation.date.isDateInPast()
						} ?: false

						val content = Evaluation.State.Content(
							evaluationId = action.evaluationId,
							attemptItems = availableAttempts.toEvaluationAttemptPickerItems(
								selectedAttempt = selectedAttempt
							),
							selectedAttempt = selectedAttempt,
							type = evaluation?.type,
							typeItems = getEvaluationTypePickerItemList(
								selectedType = evaluation?.type
							),
							scheduleMode = evaluation?.scheduleMode
								?: EvaluationScheduleMode.CONTINUOUS,
							date = evaluation?.date,
							isOverdue = isOverdue,
							grade = evaluation?.grade,
							maxGrade = evaluation?.maxGrade,
							gradeSection = getEvaluationGradeSectionItem(
								isOverdue = isOverdue,
								grade = evaluation?.grade,
								maxGrade = evaluation?.maxGrade
							)
						)

						host.processInternalEvent(
							EvaluationInternalEvent.EditorContentLoaded(
								content = content.copy(initialDraft = content.draft)
							)
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationInternalEvent.EditorLoadFailed
					)
				}
			}
		}
	}

	internal fun submit(host: MachineHost<Evaluation.Effect>, state: Evaluation.State.Content) {
		// EFSM guard over extended state: mirrors the submit button's enablement and
		// ignores re-submission while one is in flight (the old pipeline re-ran the
		// use case on a second click).
		if (!state.canSubmit) return

		if (state.evaluationId == null) {
			submitAdd(host = host, state = state)
		} else {
			submitEdit(host = host, state = state)
		}
	}

	private fun submitAdd(host: MachineHost<Evaluation.Effect>, state: Evaluation.State.Content) {
		host.launchMachineJob {
			addEvaluationUseCase.execute(state.toAddEvaluationParams()).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						EvaluationInternalEvent.SubmitStarted
					)

					is UseCaseState.Data -> host.processInternalEvent(
						EvaluationInternalEvent.SubmitSucceeded(
							message = getString(Res.string.snack_evaluation_added)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationInternalEvent.SubmitFailed(
							message = useCaseState.error.toAddSubmitErrorMessage(),
							navigateBack = false
						)
					)
				}
			}
		}
	}

	private fun submitEdit(host: MachineHost<Evaluation.Effect>, state: Evaluation.State.Content) {
		host.launchMachineJob {
			updateEvaluationUseCase.execute(state.toUpdateEvaluationParams()).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						EvaluationInternalEvent.SubmitStarted
					)

					is UseCaseState.Data -> host.processInternalEvent(
						EvaluationInternalEvent.SubmitSucceeded(
							message = getString(Res.string.snack_evaluation_updated)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						EvaluationInternalEvent.SubmitFailed(
							message = useCaseState.error.toEditSubmitErrorMessage(),
							navigateBack = useCaseState.error is UpdateEvaluationUseCaseError.NotFound
						)
					)
				}
			}
		}
	}
}
