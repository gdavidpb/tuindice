package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.record.presentation.action.ObserveQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.RefreshQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetRecordViewModeActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SelectQuarterActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import kotlinx.coroutines.flow.Flow

class RecordViewModel(
	private val observeQuartersActionProcessor: ObserveQuartersActionProcessor,
	private val refreshQuartersActionProcessor: RefreshQuartersActionProcessor,
	private val setRecordViewModeActionProcessor: SetRecordViewModeActionProcessor,
	private val selectQuarterActionProcessor: SelectQuarterActionProcessor,
	private val setSubjectGradeActionProcessor: SetSubjectGradeActionProcessor
) : BaseViewModel<Record.State, Record.Action, Record.Effect>(
	initialState = Record.State.Loading,
	initialAction = Record.Action.ObserveQuarters
) {

	fun refreshQuartersAction() =
		sendAction(Record.Action.RefreshQuarters)

	fun selectQuarterAction(quarterId: String) =
		sendAction(Record.Action.SelectQuarter(quarterId = quarterId))

	fun setViewModeAction(viewMode: RecordViewMode) =
		sendAction(Record.Action.SetViewMode(viewMode = viewMode))

	fun updateSubjectAction(
		quarterId: String,
		subjectId: String,
		grade: Int? = null,
		status: SubjectStatus? = null,
		commit: Boolean
	) =
		sendAction(
			Record.Action.SetSubjectGrade(
				quarterId = quarterId,
				subjectId = subjectId,
				grade = grade,
				status = status,
				commit = commit
			)
		)

	override suspend fun processAction(
		action: Record.Action,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return when (action) {
			is Record.Action.ObserveQuarters ->
				observeQuartersActionProcessor.process(action, sideEffect)

			is Record.Action.RefreshQuarters ->
				refreshQuartersActionProcessor.process(action, sideEffect)

			is Record.Action.SetViewMode ->
				setRecordViewModeActionProcessor.process(action, sideEffect)

			is Record.Action.SelectQuarter ->
				selectQuarterActionProcessor.process(action, sideEffect)

			is Record.Action.SetSubjectGrade ->
				setSubjectGradeActionProcessor.process(action, sideEffect)
		}
	}
}
