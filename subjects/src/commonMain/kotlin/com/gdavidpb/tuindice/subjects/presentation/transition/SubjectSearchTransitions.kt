package com.gdavidpb.tuindice.subjects.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.subjects.presentation.action.MinimumSubjectSearchQueryLength
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchInternalEvent
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchMachine

internal fun MachineDefinitionBuilder<SubjectSearch.State>.searchTransitions(
	machine: SubjectSearchMachine,
	host: MachineHost<SubjectSearch.Effect>
) {
	from<SubjectSearch.State> {
		on<SubjectSearch.Action.ObserveSubjectSearch> { state, action ->
			machine.startObservation(host = host, action = action)
			state
		}

		on<SubjectSearch.Action.UpdateQuery> { state, action ->
			if (action.query.trim().length < MinimumSubjectSearchQueryLength) {
				state.copy(
					query = action.query,
					results = emptyList(),
					isRefreshing = false,
					hasRemoteError = false
				)
			} else {
				state.copy(
					query = action.query,
					hasRemoteError = false
				)
			}
		}

		on<SubjectSearch.Action.Retry> { state, action ->
			machine.startRetry(host = host, action = action)
			state
		}

		on<SubjectSearchInternalEvent.ShortQueryCleared> { state, event ->
			state.copy(
				query = event.query,
				results = emptyList(),
				isRefreshing = false,
				hasRemoteError = false
			)
		}

		on<SubjectSearchInternalEvent.LocalResultsChanged> { state, event ->
			state.copy(
				query = event.query,
				results = event.results,
				hasRemoteError = if (event.results.isNotEmpty()) false else state.hasRemoteError
			)
		}

		on<SubjectSearchInternalEvent.RemoteSearchStarted> { state, event ->
			state.copy(
				query = event.query,
				isRefreshing = true,
				hasRemoteError = false
			)
		}

		on<SubjectSearchInternalEvent.RemoteSearchSucceeded> { state, _ ->
			state.copy(
				isRefreshing = false,
				hasRemoteError = false
			)
		}

		on<SubjectSearchInternalEvent.RemoteSearchFailed> { state, _ ->
			state.copy(
				isRefreshing = false,
				hasRemoteError = state.results.isEmpty()
			)
		}

		on<SubjectSearchInternalEvent.RetryStarted> { state, _ ->
			state.copy(
				isRefreshing = true,
				hasRemoteError = false
			)
		}

		on<SubjectSearchInternalEvent.RetryCleared> { state, _ ->
			state.copy(
				isRefreshing = false,
				hasRemoteError = false
			)
		}
	}
}
