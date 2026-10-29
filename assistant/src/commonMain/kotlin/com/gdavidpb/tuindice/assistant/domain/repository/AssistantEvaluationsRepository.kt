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
	/** Adds only the items marked selected; the unmarked rows of a batch are not run. */
	suspend fun add(args: AddEvaluationsArgs): IntentExecution

	/**
	 * Edits the evaluation only while it is still at [expectedRevision], the revision the proposal was built on: one that
	 * moved on answers CONFLICT and nothing is changed. Without an expected revision, the revision is not checked.
	 */
	suspend fun edit(args: EditEvaluationArgs, expectedRevision: Long?): IntentExecution

	/** Removes the evaluation only while it is still at [expectedRevision], as [edit] does. */
	suspend fun remove(args: RemoveEvaluationArgs, expectedRevision: Long?): IntentExecution
}
