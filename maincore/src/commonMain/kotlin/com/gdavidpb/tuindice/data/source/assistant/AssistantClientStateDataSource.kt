package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantClientStateRepository
import com.gdavidpb.tuindice.chatcontract.context.ClientState
import com.gdavidpb.tuindice.data.source.pending.PendingChangesDataSource
import com.gdavidpb.tuindice.record.domain.usecase.GetRecordRevisionUseCase

/**
 * The state of this client the assistant plans a turn with: the record revision it holds and the mutations it has not
 * seen confirmed. The port cannot fail, so a revision that cannot be read is sent as unknown (null).
 */
class AssistantClientStateDataSource(
	private val getRecordRevisionUseCase: GetRecordRevisionUseCase,
	private val pendingChangesDataSource: PendingChangesDataSource
) : AssistantClientStateRepository {
	override suspend fun clientState(): ClientState {
		return ClientState(
			recordRevision = getRecordRevisionUseCase.currentRevisionOrNull(),
			pendingMutations = pendingChangesDataSource.getPendingMutationRefs()
		)
	}
}
