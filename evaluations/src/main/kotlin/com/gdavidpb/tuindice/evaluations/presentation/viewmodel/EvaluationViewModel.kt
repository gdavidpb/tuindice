package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.CloseAddDialogActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.EditEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadAvailableSubjectsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetSubjectActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow

class EvaluationViewModel(
	private val loadAvailableSubjectsActionProcessor: LoadAvailableSubjectsActionProcessor,
	private val loadEvaluationActionProcessor: LoadEvaluationActionProcessor,
	private val addEvaluationActionProcessor: AddEvaluationActionProcessor,
	private val editEvaluationActionProcessor: EditEvaluationActionProcessor,
	private val pickGradeActionProcessor: PickGradeActionProcessor,
	private val pickMaxGradeActionProcessor: PickMaxGradeActionProcessor,
	private val closeAddDialogActionProcessor: CloseAddDialogActionProcessor,
	private val setSubjectActionProcessor: SetSubjectActionProcessor,
	private val setTypeActionProcessor: SetTypeActionProcessor,
	private val setDateActionProcessor: SetDateActionProcessor,
	private val setGradeActionProcessor: SetGradeActionProcessor,
	private val setMaxGradeActionProcessor: SetMaxGradeActionProcessor
) : BaseViewModel<Evaluation.State, Evaluation.Action, Evaluation.Effect>(initialState = Evaluation.State.Loading) {

	fun setSubjectAction(subject: Subject) =
		sendAction(Evaluation.Action.SetSubject(subject))

	fun setTypeAction(type: EvaluationType) =
		sendAction(Evaluation.Action.SetType(type))

	fun setDateAction(date: Long?) =
		sendAction(Evaluation.Action.SetDate(date))

	fun setGradeAction(grade: Double) =
		sendAction(Evaluation.Action.SetGrade(grade))

	fun setMaxGradeAction(grade: Double) =
		sendAction(Evaluation.Action.SetMaxGrade(grade))

	fun loadAvailableSubjectsAction() =
		sendAction(Evaluation.Action.LoadAvailableSubjects)

	fun loadEvaluationAction(evaluationId: String) =
		sendAction(Evaluation.Action.LoadEvaluation(evaluationId))

	fun clickAddEvaluationAction(
		subject: Subject?,
		type: EvaluationType?,
		date: Long?,
		grade: Double?,
		maxGrade: Double?
	) = sendAction(
		Evaluation.Action.ClickAddEvaluation(
			subject = subject,
			type = type,
			date = date,
			grade = grade,
			maxGrade = maxGrade
		)
	)

	fun clickEditEvaluationAction(
		evaluationId: String,
		subject: Subject?,
		type: EvaluationType?,
		date: Long?,
		grade: Double?,
		maxGrade: Double?
	) = sendAction(
		Evaluation.Action.ClickEditEvaluation(
			evaluationId = evaluationId,
			subject = subject,
			type = type,
			date = date,
			grade = grade,
			maxGrade = maxGrade
		)
	)

	fun clickGradeAction(grade: Double?, maxGrade: Double?) =
		sendAction(Evaluation.Action.ClickGrade(grade, maxGrade))

	fun clickMaxGradeAction(maxGrade: Double?) =
		sendAction(Evaluation.Action.ClickMaxGrade(maxGrade))

	fun closeDialogAction() =
		sendAction(Evaluation.Action.CloseDialog)

	override fun processAction(
		action: Evaluation.Action,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return when (action) {
			is Evaluation.Action.LoadAvailableSubjects ->
				loadAvailableSubjectsActionProcessor.process(action, sideEffect)

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

			is Evaluation.Action.CloseDialog ->
				closeAddDialogActionProcessor.process(action, sideEffect)

			is Evaluation.Action.SetSubject ->
				setSubjectActionProcessor.process(action, sideEffect)

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