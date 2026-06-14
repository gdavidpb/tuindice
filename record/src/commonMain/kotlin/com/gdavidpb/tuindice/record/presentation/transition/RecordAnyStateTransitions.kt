package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine

internal fun MachineDefinitionBuilder<Record.State>.recordAnyStateTransitions(
	machine: RecordMachine,
	host: MachineHost<Record.Effect>
) {
	fromAny {
		on<Record.Action.RefreshRecord> { state, _ ->
			machine.refreshRecord(host = host)
			state
		}

		on<Record.Action.SetViewMode> { state, action ->
			machine.setViewMode(host = host, viewMode = action.viewMode)
			state
		}

		on<Record.Action.UpsertAttemptSelection> { state, action ->
			machine.upsertAttemptSelection(
				host = host,
				attemptId = action.attemptId,
				grade = action.grade,
				outcome = action.outcome,
				commit = action.commit
			)
			state
		}

		on<Record.Action.DeleteSyntheticTerm> { state, action ->
			machine.deleteSyntheticTerm(host = host, termId = action.termId)
			state
		}

		onTo<RecordInternalEvent.RecordContentObserved, Record.State.Content> { _, event ->
			Record.State.Content(
				viewMode = event.viewMode,
				record = event.record,
				selectedTermId = event.selectedTermId
			)
		}

		onTo<RecordInternalEvent.RecordEmptyObserved, Record.State.Empty> { _, _ ->
			Record.State.Empty
		}

		onTo<RecordInternalEvent.RecordWaitingObserved, Record.State.Loading> { _, _ ->
			Record.State.Loading
		}

		onTo<RecordInternalEvent.RecordObservationFailed, Record.State.Failed> { _, _ ->
			Record.State.Failed
		}

		onTo<RecordInternalEvent.RecordRefreshStarted, Record.State.Loading> { _, _ ->
			Record.State.Loading
		}

		onTo<RecordInternalEvent.RecordRefreshFailed, Record.State.Failed>(
			emits = setOf(Record.Effect.NavigateToOutdatedCredentials::class)
		) { _, event ->
			if (event.navigateToOutdatedCredentials) {
				host.sendEffect(Record.Effect.NavigateToOutdatedCredentials)
			}

			Record.State.Failed
		}

		on<RecordInternalEvent.RecordViewModeSet>(
			emits = setOf(Record.Effect.ShowTopBarBanner::class)
		) { state, event ->
			host.sendEffect(
				Record.Effect.ShowTopBarBanner(
					viewMode = event.viewMode,
					behavior = TopBarBannerBehavior.AutoDismiss(
						RECORD_VIEW_MODE_BANNER_AUTO_DISMISS_MILLIS
					)
				)
			)

			state
		}

		on<RecordInternalEvent.RecordUnauthorized>(
			emits = setOf(Record.Effect.NavigateToOutdatedCredentials::class)
		) { state, _ ->
			host.sendEffect(Record.Effect.NavigateToOutdatedCredentials)

			state
		}

		on<RecordInternalEvent.SyntheticTermDeleted>(
			emits = setOf(Record.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Record.Effect.ShowSnackBar(message = event.message))

			state
		}

		on<RecordInternalEvent.SyntheticTermDeleteFailed>(
			emits = setOf(
				Record.Effect.ShowSnackBar::class,
				Record.Effect.NavigateToOutdatedCredentials::class
			)
		) { state, event ->
			if (event.navigateToOutdatedCredentials) {
				host.sendEffect(Record.Effect.NavigateToOutdatedCredentials)
			}

			host.sendEffect(Record.Effect.ShowSnackBar(message = event.message))

			state
		}
	}
}

private const val RECORD_VIEW_MODE_BANNER_AUTO_DISMISS_MILLIS = 5_000L
