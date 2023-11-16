package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.evaluations.presentation.action.add.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.CloseAddDialogActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.LoadAvailableSubjectsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetSubjectActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow

class AddEvaluationViewModel(
	private val loadAvailableSubjectsActionProcessor: LoadAvailableSubjectsActionProcessor,
	private val addEvaluationActionProcessor: AddEvaluationActionProcessor,
	private val pickGradeActionProcessor: PickGradeActionProcessor,
	private val pickMaxGradeActionProcessor: PickMaxGradeActionProcessor,
	private val closeAddDialogActionProcessor: CloseAddDialogActionProcessor,
	private val setSubjectActionProcessor: SetSubjectActionProcessor,
	private val setTypeActionProcessor: SetTypeActionProcessor,
	private val setDateActionProcessor: SetDateActionProcessor,
	private val setGradeActionProcessor: SetGradeActionProcessor,
	private val setMaxGradeActionProcessor: SetMaxGradeActionProcessor
) : BaseViewModel<AddEvaluation.State, AddEvaluation.Action, AddEvaluation.Effect>(initialState = AddEvaluation.State.Loading) {

	fun setSubjectAction(subject: Subject) =
		sendAction(AddEvaluation.Action.SetSubject(subject))

	fun setTypeAction(type: EvaluationType) =
		sendAction(AddEvaluation.Action.SetType(type))

	fun setDateAction(date: Long?) =
		sendAction(AddEvaluation.Action.SetDate(date))

	fun setGradeAction(grade: Double?) =
		sendAction(AddEvaluation.Action.SetGrade(grade))

	fun setMaxGradeAction(grade: Double?) =
		sendAction(AddEvaluation.Action.SetMaxGrade(grade))

	fun loadAvailableSubjectsAction() =
		sendAction(AddEvaluation.Action.LoadAvailableSubjects)

	fun clickDoneAction(
		subject: Subject?,
		type: EvaluationType?,
		date: Long?,
		grade: Double?,
		maxGrade: Double?
	) = sendAction(
		AddEvaluation.Action.ClickDone(
			subject = subject,
			type = type,
			date = date,
			grade = grade,
			maxGrade = maxGrade
		)
	)

	fun clickGradeAction(grade: Double?, maxGrade: Double?) =
		sendAction(AddEvaluation.Action.ClickGrade(grade ?: 0.0, maxGrade ?: 0.0))

	fun clickMaxGradeAction(grade: Double?) =
		sendAction(AddEvaluation.Action.ClickMaxGrade(grade ?: 0.0))

	fun closeDialogAction() =
		sendAction(AddEvaluation.Action.CloseDialog)

	override fun processAction(
		action: AddEvaluation.Action,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		return when (action) {
			is AddEvaluation.Action.LoadAvailableSubjects ->
				loadAvailableSubjectsActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.ClickDone ->
				addEvaluationActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.ClickGrade ->
				pickGradeActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.ClickMaxGrade ->
				pickMaxGradeActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.CloseDialog ->
				closeAddDialogActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.SetSubject ->
				setSubjectActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.SetType ->
				setTypeActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.SetDate ->
				setDateActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.SetGrade ->
				setGradeActionProcessor.process(action, sideEffect)

			is AddEvaluation.Action.SetMaxGrade ->
				setMaxGradeActionProcessor.process(action, sideEffect)
		}
	}
}