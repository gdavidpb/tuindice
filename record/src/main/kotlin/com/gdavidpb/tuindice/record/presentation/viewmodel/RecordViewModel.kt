package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.record.presentation.action.LoadQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.UpdateSubjectActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow

class RecordViewModel(
	private val loadQuartersActionProcessor: LoadQuartersActionProcessor,
	private val updateSubjectActionProcessor: UpdateSubjectActionProcessor
) : BaseViewModel<Record.State, Record.Action, Record.Effect>(initialState = Record.State.Loading) {

	fun loadQuartersAction() =
		sendAction(Record.Action.LoadQuarters)

	fun updateSubjectAction(subjectId: String, grade: Int, dispatchToRemote: Boolean) =
		sendAction(
			Record.Action.UpdateSubject(
				subjectId = subjectId,
				grade = grade,
				dispatchToRemote = dispatchToRemote
			)
		)

	override fun processAction(
		action: Record.Action,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return when (action) {
			is Record.Action.LoadQuarters ->
				loadQuartersActionProcessor.process(action, sideEffect)

			is Record.Action.UpdateSubject ->
				updateSubjectActionProcessor.process(action, sideEffect)
		}
	}
}