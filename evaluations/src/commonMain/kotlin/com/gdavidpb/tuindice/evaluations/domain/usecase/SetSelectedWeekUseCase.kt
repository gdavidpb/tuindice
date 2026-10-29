package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.model.SelectedWeekPick
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

class SetSelectedWeekUseCase(
	private val evaluationsSelectionRepository: EvaluationsSelectionRepository,
	override val reportingRepository: ReportingRepository,
	private val sessionCoroutineScope: SessionCoroutineScope
) : FlowUseCase<SelectedWeekPick, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: SelectedWeekPick): Flow<Unit> {
		// The pick is saved in the session, so leaving the screen does not cancel the save.
		val save = sessionCoroutineScope.async {
			evaluationsSelectionRepository.saveSelectedWeekPick(pick = params)
		}

		return flow {
			save.await()
			emit(Unit)
		}.onCompletion { cause ->
			// A caller that left before the save ended does not await it, so a failure of the save is reported here.
			if (cause is CancellationException) {
				save.invokeOnCompletion { writeCause ->
					// A save cancelled by the end of the session is not a failure.
					if (writeCause != null && writeCause !is CancellationException) reportHandledException(writeCause)
				}
			}
		}
	}
}
