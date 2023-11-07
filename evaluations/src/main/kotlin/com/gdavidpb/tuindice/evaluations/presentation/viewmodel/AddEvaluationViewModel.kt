package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toAddEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.reducer.AddEvaluationReducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.AvailableSubjectsReducer
import com.gdavidpb.tuindice.evaluations.utils.extension.isOverdue

class AddEvaluationViewModel(
	private val getAvailableSubjectsUseCase: GetAvailableSubjectsUseCase,
	private val availableSubjectsReducer: AvailableSubjectsReducer,
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val addEvaluationReducer: AddEvaluationReducer
) : BaseViewModel<AddEvaluation.State, AddEvaluation.Action, AddEvaluation.Event>(initialViewState = AddEvaluation.State.Loading) {

	fun setSubject(subject: Subject) {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Content) return

		setState(
			currentState.copy(
				subject = subject
			)
		)
	}

	fun setType(type: EvaluationType) {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Content) return

		setState(
			currentState.copy(
				type = type
			)
		)
	}

	fun setDate(date: Long?) {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Content) return

		setState(
			currentState.copy(
				date = date,
				isOverdue = date.isOverdue()
			)
		)
	}

	fun setGrade(grade: Double?) {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Content) return

		setState(
			currentState.copy(
				grade = grade
			)
		)
	}

	fun setMaxGrade(grade: Double?) {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Content) return

		setState(
			currentState.copy(
				maxGrade = grade
			)
		)
	}

	fun loadAvailableSubjectsAction() =
		emitAction(AddEvaluation.Action.LoadAvailableSubjects)

	fun clickDoneAction() {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Content) return

		emitAction(
			AddEvaluation.Action.ClickDone(
				subject = currentState.subject,
				type = currentState.type,
				date = currentState.date,
				grade = currentState.grade,
				maxGrade = currentState.maxGrade
			)
		)
	}

	fun clickGradeAction(grade: Double?, maxGrade: Double?) =
		emitAction(AddEvaluation.Action.ClickGrade(grade ?: 0.0, maxGrade ?: 0.0))

	fun clickMaxGradeAction(grade: Double?) =
		emitAction(AddEvaluation.Action.ClickMaxGrade(grade ?: 0.0))

	fun closeDialogAction() =
		emitAction(AddEvaluation.Action.CloseDialog)

	override suspend fun reducer(action: AddEvaluation.Action) {
		when (action) {
			is AddEvaluation.Action.LoadAvailableSubjects ->
				getAvailableSubjectsUseCase
					.execute(
						params = Unit
					)
					.collect(viewModel = this, reducer = availableSubjectsReducer)

			is AddEvaluation.Action.ClickDone ->
				addEvaluationUseCase
					.execute(
						params = action.toAddEvaluationParams()
					)
					.collect(viewModel = this, reducer = addEvaluationReducer)

			is AddEvaluation.Action.ClickGrade ->
				sendEvent(
					AddEvaluation.Event.ShowGradePickerDialog(
						grade = action.grade,
						maxGrade = action.maxGrade
					)
				)

			is AddEvaluation.Action.ClickMaxGrade ->
				sendEvent(
					AddEvaluation.Event.ShowMaxGradePickerDialog(
						grade = action.grade
					)
				)

			is AddEvaluation.Action.CloseDialog ->
				sendEvent(AddEvaluation.Event.CloseDialog)
		}
	}
}