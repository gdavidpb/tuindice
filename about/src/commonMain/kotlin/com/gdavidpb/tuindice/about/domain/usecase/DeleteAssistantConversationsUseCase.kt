package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.AssistantConversationsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

// The deletion runs in the session, like the consent write: a caller that leaves in the middle of its two steps
// (the server, then the device) does not leave them apart. The flow only waits for the deletion.
class DeleteAssistantConversationsUseCase(
	private val assistantConversationsRepository: AssistantConversationsRepository,
	private val sessionCoroutineScope: SessionCoroutineScope,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		val deletion = sessionCoroutineScope.async {
			assistantConversationsRepository.deleteAssistantConversations()
		}

		return flow {
			deletion.await()
			emit(Unit)
		}.onCompletion { cause ->
			// A caller that left before the deletion ended does not await it, so a failure of the deletion is reported here.
			// A failure that reaches the caller is reported by FlowUseCase, and only once.
			if (cause is CancellationException) {
				deletion.invokeOnCompletion { deletionCause ->
					// A deletion cancelled by the end of the session is not a failure.
					if (deletionCause != null && deletionCause !is CancellationException) {
						reportHandledException(deletionCause)
					}
				}
			}
		}
	}
}
