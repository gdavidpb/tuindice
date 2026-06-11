package com.gdavidpb.tuindice.subjects.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.mapper.withSelectedTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SelectSubjectSegmentTabActionProcessor : ActionProcessor<
	SubjectDetail.State,
	SubjectDetail.Action.SelectSubjectSegmentTab,
	SubjectDetail.Effect
	> {
	override suspend fun process(
		action: SubjectDetail.Action.SelectSubjectSegmentTab,
		sideEffect: (SubjectDetail.Effect) -> Unit
	): Flow<Mutation<SubjectDetail.State>> {
		return flowOf(
			suspend { state: SubjectDetail.State ->
				when (state) {
					is SubjectDetail.State.Content ->
						state.copy(detail = state.detail.withSelectedTab(action.tab))

					is SubjectDetail.State.Failed,
					SubjectDetail.State.Idle,
					SubjectDetail.State.Loading,
					is SubjectDetail.State.Unavailable,
					-> state
				}
			}
		)
	}
}
