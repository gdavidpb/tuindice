package com.gdavidpb.tuindice.assistant.data.repository

import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import kotlinx.coroutines.flow.Flow

/** Events of one turn, in order, from [afterSeq] on. The flow completes after a run_finished or a run_error. */
interface AssistantEventsDataRepository {
	fun events(turnId: String, afterSeq: Long): Flow<AssistantEvent>
}
