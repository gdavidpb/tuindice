package com.gdavidpb.tuindice.assistant.data.repository

import com.gdavidpb.tuindice.assistant.data.model.AssistantStreamEvent
import kotlinx.coroutines.flow.Flow

/**
 * Events of one turn, in order, from [afterSeq] on. The flow completes after a run_finished or a run_error, or when the
 * connections give up before either: the turn's result is then unknown, and no event stands in for it.
 */
interface AssistantEventsDataRepository {
	fun events(turnId: String, afterSeq: Long): Flow<AssistantStreamEvent>
}
