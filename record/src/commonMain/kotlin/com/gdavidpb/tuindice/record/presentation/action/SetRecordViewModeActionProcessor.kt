package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SetRecordViewModeActionProcessor(
	private val setRecordViewModeUseCase: SetRecordViewModeUseCase
) : ActionProcessor<Record.State, Record.Action.SetViewMode, Record.Effect> {
	override suspend fun process(
		action: Record.Action.SetViewMode,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return setRecordViewModeUseCase.execute(action.viewMode)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> {
						sideEffect(
							Record.Effect.ShowTopBarBanner(
								viewMode = action.viewMode,
								behavior = TopBarBannerBehavior.AutoDismiss(
									RECORD_VIEW_MODE_BANNER_AUTO_DISMISS_MILLIS
								)
							)
						)

						suspend { state: Record.State ->
							state
						}
					}

					is UseCaseState.Error,
					is UseCaseState.Loading,
					-> suspend { state: Record.State ->
						state
					}
				}
			}
	}
}

private const val RECORD_VIEW_MODE_BANNER_AUTO_DISMISS_MILLIS = 5_000L
