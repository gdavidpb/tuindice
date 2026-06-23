package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine

class RecordViewModel(
	override val screenMachine: RecordMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Record.State, Record.Action, Record.Effect>(
	name = "record",
	initialState = screenMachine.initialState(),
	initialAction = Record.Action.ObserveRecord,
	dispatchers = dispatchers
) {
	fun ensureRecordLoadedAction() {
		sendAction(Record.Action.EnsureRecordLoaded)
	}

	fun refreshRecordAction() {
		sendAction(Record.Action.RefreshRecord)
	}

	fun selectTermAction(termId: String) {
		sendAction(Record.Action.SelectTerm(termId))
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
}
