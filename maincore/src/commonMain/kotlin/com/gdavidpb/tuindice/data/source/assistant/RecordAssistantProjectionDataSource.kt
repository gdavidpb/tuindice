package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantProjectionRepository
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.UpsertAttemptSelectionParams

/**
 * The grade and outcome the student projects for an attempt. `clear` removes the projection: nothing is projected
 * in its place. The outcome travels by its serial name (such as "approved"), as academiccore writes it.
 */
class RecordAssistantProjectionDataSource(
	private val upsertAttemptSelectionUseCase: UpsertAttemptSelectionUseCase,
	private val recordIntentRunner: RecordIntentRunner
) : AssistantProjectionRepository {
	override suspend fun save(args: ProjectionArgs, expectedRevision: Long?): IntentExecution {
		return guardIntent { saveProjection(args, expectedRevision) }
	}

	private suspend fun saveProjection(args: ProjectionArgs, expectedRevision: Long?): IntentExecution {
		val outcome = args.outcome?.let { serialName -> enumBySerialName<AttemptOutcome>(serialName) }

		val refusal = invalidProjection(args, outcome) ?: recordIntentRunner.revisionConflictOrNull(expectedRevision)
		if (refusal != null) return refusal

		val grade = if (args.clear) null else args.grade
		val projectedOutcome = if (args.clear) null else outcome

		return recordIntentRunner.runMutation(args.mutationId) {
			upsertAttemptSelectionUseCase.execute(
				UpsertAttemptSelectionParams(
					attemptId = args.attemptId,
					grade = grade,
					outcome = projectedOutcome,
					mutationId = args.mutationId
				)
			).settled()
		}
	}

	// An outcome the app does not know is refused, and so is a projection that projects nothing. Clearing is the one
	// projection that projects nothing by design.
	private fun invalidProjection(args: ProjectionArgs, outcome: AttemptOutcome?): IntentExecution? {
		val unknownOutcome = args.outcome != null && outcome == null
		val projectsNothing = !args.clear && args.grade == null && outcome == null

		return if (unknownOutcome || projectsNothing) invalidArgs() else null
	}
}
