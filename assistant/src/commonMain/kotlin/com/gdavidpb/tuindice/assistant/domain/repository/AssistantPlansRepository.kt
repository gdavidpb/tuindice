package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.chatcontract.intent.CreatePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditPlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemovePlanArgs

/**
 * Implemented by maincore. [expectedRevision] is the record revision the intent was built on. It answers FAILED
 * with its detail code and never throws, as the evaluations port does.
 */
interface AssistantPlansRepository {
	suspend fun create(args: CreatePlanArgs, expectedRevision: Long?): IntentExecution

	suspend fun edit(args: EditPlanArgs, expectedRevision: Long?): IntentExecution

	suspend fun remove(args: RemovePlanArgs, expectedRevision: Long?): IntentExecution
}
