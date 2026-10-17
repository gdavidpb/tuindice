package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs

/**
 * Implemented by maincore. It runs the change an intent asked for and answers how it ended. A failure is an answer,
 * FAILED with its detail code, and never an exception: the use cases do not catch.
 */
interface AssistantEvaluationsRepository {
	suspend fun add(args: AddEvaluationsArgs): IntentExecution

	suspend fun edit(args: EditEvaluationArgs): IntentExecution

	suspend fun remove(args: RemoveEvaluationArgs): IntentExecution
}
