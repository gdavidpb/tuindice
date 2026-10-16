package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.record.domain.mapper.attemptSelectionToOverridePayload
import com.gdavidpb.tuindice.record.domain.model.filteredProjectionFor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.presentation.mapper.hasScheduleOnTerm
import com.gdavidpb.tuindice.record.presentation.mapper.isCurrentTerm
import com.gdavidpb.tuindice.record.presentation.mapper.isSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.mapper.resolveRecordEmpty
import com.gdavidpb.tuindice.record.presentation.mapper.resolveRecordFailed

internal fun MachineDefinitionBuilder<Record.State>.recordAnyStateTransitions(
	machine: RecordMachine,
	host: MachineHost<Record.Effect>
) {
	fromAny {
		on<Record.Action.EnsureRecordLoaded> { state, _ ->
			machine.ensureRecordLoaded(host = host)
			state
		}

		on<Record.Action.RefreshRecord> { state, _ ->
			machine.refreshRecord(host = host)
			state
		}

		on<Record.Action.SetViewMode> { state, action ->
			machine.setViewMode(host = host, viewMode = action.viewMode)
			state
		}

		on<Record.Action.UpsertAttemptSelection> { state, action ->
			if (action.commit) {
				machine.upsertAttemptSelection(
					host = host,
					attemptId = action.attemptId,
					grade = action.grade,
					outcome = action.outcome
				)
			}

			val contentState = state as? Record.State.Content ?: return@on state

			val payload = runCatching {
				attemptSelectionToOverridePayload(
					grade = action.grade,
					outcome = action.outcome
				)
			}.getOrNull() ?: return@on state

			contentState.copy(
				inFlightSelection = Record.State.InFlightSelection(
					override = AttemptOverride(
						attemptId = action.attemptId,
						score = payload.first,
						outcome = payload.second,
						updatedAtMillis = currentTimeMillis()
					),
					isCommitted = action.commit
				)
			)
		}

		on<Record.Action.DeleteSyntheticTerm> { state, action ->
			machine.deleteSyntheticTerm(host = host, termId = action.termId)
			state
		}

		onTo<RecordInternalEvent.RecordContentObserved, Record.State.Content> { state, event ->
			val selectedTermId = state.selectedTermIdFor(event)

			Record.State.Content(
				viewMode = event.viewMode,
				record = event.record,
				selectedTermId = selectedTermId,
				inFlightSelection = (state as? Record.State.Content)
					?.inFlightSelection
					?.takeUnless { selection ->
						selection.isCommitted && event.record.hasSettled(selection.override)
					},
				notice = event.notice,
				hasSelectedTermSchedule = event.record.hasScheduleOnTerm(termId = selectedTermId),
				isSelectedTermCurrent = event.record.isCurrentTerm(termId = selectedTermId),
				isSelectedTermSynthetic = event.record.isSyntheticTerm(termId = selectedTermId),
				storedSelectedTermId = event.selectedTermId,
				// A jump still waiting for the list to take it is kept: an observation must not cost it.
				arrivalAttemptId = (state as? Record.State.Content)?.arrivalAttemptId
			)
		}

		onTo<RecordInternalEvent.RecordEmptyObserved, Record.State.Empty> { _, event ->
			resolveRecordEmpty(notice = event.notice)
		}

		onTo<RecordInternalEvent.RecordWaitingObserved, Record.State.Loading> { _, _ ->
			Record.State.Loading
		}

		// Only Failed shows it. Elsewhere there is nothing to keep: a refresh that fails later asks
		// again, and brings the answer in its own event.
		on<RecordInternalEvent.NewStudentNoRecordObserved> { state, _ -> state }

		onTo<RecordInternalEvent.RecordObservationFailed, Record.State.Failed> { _, _ ->
			resolveRecordFailed(isNewStudentNoRecord = false)
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

			resolveRecordFailed(isNewStudentNoRecord = event.isNewStudentNoRecord)
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

		on<RecordInternalEvent.AttemptSelectionFailed>(
			emits = setOf(Record.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Record.Effect.ShowSnackBar(message = event.message))

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

		on<RecordInternalEvent.SyntheticTermRejected>(
			emits = setOf(Record.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Record.Effect.ShowSnackBar(message = event.message))

			state
		}
	}
}

// The term the content shows once an observation brings [event]. The store's term, unless this visit chose
// another one (a tap or a jump) and the store has not changed since: then the visit's term is kept while the
// view mode still lists it. A store that changed (another term chosen, a new term, another view mode) wins.
internal fun Record.State.selectedTermIdFor(event: RecordInternalEvent.RecordContentObserved): String {
	val content = this as? Record.State.Content ?: return event.selectedTermId

	val keepsVisitSelection = content.selectedTermId != event.selectedTermId &&
		content.viewMode == event.viewMode &&
		content.storedSelectedTermId == event.selectedTermId &&
		event.record.filteredProjectionFor(event.viewMode).terms.any { term ->
			term.id == content.selectedTermId
		}

	return if (keepsVisitSelection) content.selectedTermId else event.selectedTermId
}

private fun AcademicRecord.hasSettled(override: AttemptOverride): Boolean {
	val currentOverride = attemptOverrides.firstOrNull { candidate ->
		candidate.attemptId == override.attemptId
	}

	if (currentOverride != null) {
		return currentOverride.score == override.score &&
			currentOverride.outcome == override.outcome
	}

	val attempt = terms
		.flatMap(AcademicTerm::attempts)
		.firstOrNull { candidate -> candidate.id == override.attemptId }

	return attempt != null &&
		attempt.academicScore == override.score &&
		attempt.academicOutcome == (override.outcome ?: attempt.academicOutcome)
}

private const val RECORD_VIEW_MODE_BANNER_AUTO_DISMISS_MILLIS = 5_000L
