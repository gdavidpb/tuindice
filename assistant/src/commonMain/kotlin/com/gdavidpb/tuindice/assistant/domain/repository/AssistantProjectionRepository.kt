package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs

/**
 * Implemented by maincore. [expectedRevision] is the record revision the intent was built on. It answers FAILED
 * with its detail code and never throws, as the evaluations port does.
 */
interface AssistantProjectionRepository {
	suspend fun save(args: ProjectionArgs, expectedRevision: Long?): IntentExecution
}
