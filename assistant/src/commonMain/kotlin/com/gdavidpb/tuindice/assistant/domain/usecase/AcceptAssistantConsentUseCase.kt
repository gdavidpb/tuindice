package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

// The write runs in the session, not in the caller's scope, as the about module's SetAssistantConsentUseCase does: a
// sheet that closes while the server answers does not cut the write short, so the device keeps the consent it took.
// The flow only waits for the write and reports its outcome.
class AcceptAssistantConsentUseCase(
	private val consentRepository: AssistantConsentRepository,
	private val sessionCoroutineScope: SessionCoroutineScope,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Boolean, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Boolean): Flow<Unit> {
		val write = sessionCoroutineScope.async {
			consentRepository.setAssistantConsentGranted(granted = params)
		}

		return flow {
			write.await()
			emit(Unit)
		}.onCompletion { cause ->
			// A caller that left before the write ended does not await it, so a failure of the write is reported here.
			// A failure that reaches the caller is reported by FlowUseCase, and only once.
			if (cause is CancellationException) {
				write.invokeOnCompletion { writeCause ->
					// A write cancelled by the end of the session is not a failure.
					if (writeCause != null && writeCause !is CancellationException) reportHandledException(writeCause)
				}
			}
		}
	}
}
