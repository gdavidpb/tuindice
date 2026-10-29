package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

// The answer of a use call once it has one: its Data or its Error. The Loading a call starts with is skipped.
internal suspend fun <T, E : UseCaseError> Flow<UseCaseState<T, E>>.settled(): UseCaseState<T, E> {
	return first { state -> state !is UseCaseState.Loading }
}

// The outcomes an observe use case emits, without its states.
internal fun <E : UseCaseError> Flow<UseCaseState<MutationOutcome, E>>.outcomes(): Flow<MutationOutcome> {
	return mapNotNull { state ->
		when (state) {
			is UseCaseState.Data -> state.value
			else -> null
		}
	}
}
