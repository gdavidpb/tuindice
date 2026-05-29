package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.action.DeleteSyntheticTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.ObserveRecordActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.RefreshRecordActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SelectRecordTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetRecordViewModeActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.UpsertAttemptSelectionActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow

class RecordViewModel(
	private val observeRecordActionProcessor: ObserveRecordActionProcessor,
	private val refreshRecordActionProcessor: RefreshRecordActionProcessor,
	private val setRecordViewModeActionProcessor: SetRecordViewModeActionProcessor,
	private val selectRecordTermActionProcessor: SelectRecordTermActionProcessor,
	private val upsertAttemptSelectionActionProcessor: UpsertAttemptSelectionActionProcessor,
	private val deleteSyntheticTermActionProcessor: DeleteSyntheticTermActionProcessor,
	override val eventPublisher: EventPublisher
) : BaseViewModel<Record.State, Record.Action, Record.Effect>(
	name = "record",
	initialState = Record.State.Idle,
	initialAction = Record.Action.ObserveRecord
) {
	fun refreshRecordAction() {
		sendAction(Record.Action.RefreshRecord)
	}

	fun selectTermAction(termId: String) {
		val currentViewMode = (state.value as? Record.State.Content)?.viewMode ?: return

		sendAction(
			Record.Action.SelectTerm(
				termId = termId,
				viewMode = currentViewMode
			)
		)
	}

	fun setViewModeAction(viewMode: RecordViewMode) {
		sendAction(Record.Action.SetViewMode(viewMode))
	}

	fun upsertAttemptSelectionAction(
		attemptId: String,
		grade: Int? = null,
		outcome: AttemptOutcome? = null,
		commit: Boolean
	) {
		sendAction(
			Record.Action.UpsertAttemptSelection(
				attemptId = attemptId,
				grade = grade,
				outcome = outcome,
				commit = commit
			)
		)
	}

	fun deleteSyntheticTermAction(termId: String) {
		sendAction(Record.Action.DeleteSyntheticTerm(termId))
	}

	override suspend fun processAction(
		action: Record.Action,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return when (action) {
			is Record.Action.ObserveRecord ->
				observeRecordActionProcessor.process(action, sideEffect)

			is Record.Action.RefreshRecord ->
				refreshRecordActionProcessor.process(action, sideEffect)

			is Record.Action.SetViewMode ->
				setRecordViewModeActionProcessor.process(action, sideEffect)

			is Record.Action.SelectTerm ->
				selectRecordTermActionProcessor.process(action, sideEffect)

			is Record.Action.UpsertAttemptSelection ->
				upsertAttemptSelectionActionProcessor.process(action, sideEffect)

			is Record.Action.DeleteSyntheticTerm ->
				deleteSyntheticTermActionProcessor.process(action, sideEffect)
		}
	}
}
