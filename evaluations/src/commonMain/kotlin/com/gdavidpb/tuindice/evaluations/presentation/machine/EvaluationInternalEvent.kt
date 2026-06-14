package com.gdavidpb.tuindice.evaluations.presentation.machine

import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation

/**
 * Internal machine inputs for the evaluation editor: the load lifecycle (attempts-only
 * for add mode, evaluation plus attempts for edit mode, both landing as a prepared
 * Content) and the submit lifecycle.
 */
sealed interface EvaluationInternalEvent {
	data object EditorLoadStarted : EvaluationInternalEvent

	data class EditorContentLoaded(
		val content: Evaluation.State.Content
	) : EvaluationInternalEvent

	data object EditorLoadFailed : EvaluationInternalEvent

	data object SubmitStarted : EvaluationInternalEvent

	data class SubmitSucceeded(
		val message: String
	) : EvaluationInternalEvent

	data class SubmitFailed(
		val message: String,
		val navigateBack: Boolean
	) : EvaluationInternalEvent
}
