package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.record.presentation.action.LoadQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow

class RecordViewModel(
	private val loadQuartersActionProcessor: LoadQuartersActionProcessor,
	private val setSubjectGradeActionProcessor: SetSubjectGradeActionProcessor
) : BaseViewModel<Record.State, Record.Action, Record.Effect>(initialState = Record.State.Loading) {

	fun loadQuartersAction() =
		sendAction(Record.Action.LoadQuarters)

	fun updateSubjectAction(
		quarterId: String,
		subjectId: String,
		grade: Int,
		commit: Boolean
	) =
		sendAction(
			Record.Action.SetSubjectGrade(
				quarterId = quarterId,
				subjectId = subjectId,
				grade = grade,
				commit = commit
			)
		)

	override suspend fun processAction(
		action: Record.Action,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return when (action) {
			is Record.Action.LoadQuarters ->
				loadQuartersActionProcessor.process(action, sideEffect)

			is Record.Action.SetSubjectGrade ->
				setSubjectGradeActionProcessor.process(action, sideEffect)
		}
	}
}