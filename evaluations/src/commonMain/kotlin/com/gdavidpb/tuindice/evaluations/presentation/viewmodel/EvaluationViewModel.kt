package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.EditEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadAvailableAttemptsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetAttemptActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow

class EvaluationViewModel(
	private val loadAvailableAttemptsActionProcessor: LoadAvailableAttemptsActionProcessor,
	private val loadEvaluationActionProcessor: LoadEvaluationActionProcessor,
	private val addEvaluationActionProcessor: AddEvaluationActionProcessor,
	private val editEvaluationActionProcessor: EditEvaluationActionProcessor,
	private val pickGradeActionProcessor: PickGradeActionProcessor,
	private val pickMaxGradeActionProcessor: PickMaxGradeActionProcessor,
	private val setAttemptActionProcessor: SetAttemptActionProcessor,
	private val setTypeActionProcessor: SetTypeActionProcessor,
	private val setDateActionProcessor: SetDateActionProcessor,
	private val setGradeActionProcessor: SetGradeActionProcessor,
	private val setMaxGradeActionProcessor: SetMaxGradeActionProcessor,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : BaseViewModel<Evaluation.State, Evaluation.Action, Evaluation.Effect>(
	name = "evaluation",
	initialState = Evaluation.State.Loading,
	dispatchers = dispatchers
) {
	fun setAttemptAction(attempt: EditableAttemptDescriptor?) =
		sendAction(Evaluation.Action.SetAttempt(attempt))

	fun setTypeAction(type: EvaluationType?) =
		sendAction(Evaluation.Action.SetType(type))

	fun setDateAction(date: Long?) =
		sendAction(Evaluation.Action.SetDate(date))

	fun setGradeAction(grade: Double) =
		sendAction(Evaluation.Action.SetGrade(grade))

	fun setMaxGradeAction(grade: Double) =
		sendAction(Evaluation.Action.SetMaxGrade(grade))

	fun loadAvailableAttemptsAction() =
		sendAction(Evaluation.Action.LoadAvailableAttempts)

	fun loadEvaluationAction(evaluationId: String) =
		sendAction(Evaluation.Action.LoadEvaluation(evaluationId))

	fun clickAddEvaluationAction(
		attempt: EditableAttemptDescriptor?,
		type: EvaluationType?,
		scheduleMode: EvaluationScheduleMode,
		date: Long?,
		grade: Double?,
		maxGrade: Double?
	) = sendAction(
		Evaluation.Action.ClickAddEvaluation(
			attempt = attempt,
			type = type,
			scheduleMode = scheduleMode,
			date = date,
			grade = grade,
			maxGrade = maxGrade
		)
	)

	fun clickEditEvaluationAction(
		evaluationId: String,
		attempt: EditableAttemptDescriptor?,
		type: EvaluationType?,
		scheduleMode: EvaluationScheduleMode,
		date: Long?,
		grade: Double?,
		maxGrade: Double?
	) = sendAction(
		Evaluation.Action.ClickEditEvaluation(
			evaluationId = evaluationId,
			attempt = attempt,
			type = type,
			scheduleMode = scheduleMode,
			date = date,
			grade = grade,
			maxGrade = maxGrade
		)
	)

	fun clickGradeAction(evaluationName: String, subjectCode: String, grade: Double?, maxGrade: Double?) =
		sendAction(Evaluation.Action.ClickGrade(evaluationName, subjectCode, grade, maxGrade))

	fun clickMaxGradeAction(evaluationName: String, subjectCode: String, maxGrade: Double?) =
		sendAction(Evaluation.Action.ClickMaxGrade(evaluationName, subjectCode, maxGrade))

	override suspend fun processAction(
		action: Evaluation.Action,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return when (action) {
			is Evaluation.Action.LoadAvailableAttempts ->
				loadAvailableAttemptsActionProcessor.process(action, sideEffect)

			is Evaluation.Action.LoadEvaluation ->
				loadEvaluationActionProcessor.process(action, sideEffect)

			is Evaluation.Action.ClickAddEvaluation ->
				addEvaluationActionProcessor.process(action, sideEffect)

			is Evaluation.Action.ClickEditEvaluation ->
				editEvaluationActionProcessor.process(action, sideEffect)

			is Evaluation.Action.ClickGrade ->
				pickGradeActionProcessor.process(action, sideEffect)

			is Evaluation.Action.ClickMaxGrade ->
				pickMaxGradeActionProcessor.process(action, sideEffect)

			is Evaluation.Action.SetAttempt ->
				setAttemptActionProcessor.process(action, sideEffect)

			is Evaluation.Action.SetType ->
				setTypeActionProcessor.process(action, sideEffect)

			is Evaluation.Action.SetDate ->
				setDateActionProcessor.process(action, sideEffect)

			is Evaluation.Action.SetGrade ->
				setGradeActionProcessor.process(action, sideEffect)

			is Evaluation.Action.SetMaxGrade ->
				setMaxGradeActionProcessor.process(action, sideEffect)
		}
	}
}
