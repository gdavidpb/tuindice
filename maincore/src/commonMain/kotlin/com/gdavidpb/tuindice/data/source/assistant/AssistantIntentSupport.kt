package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.CancellationException

// How long an intent waits for the outcomes of its mutations. A mutation still running after it is QUEUED: it stays in
// the outbox and its outcome comes later, but the answer does not wait for it.
internal const val MUTATION_OUTCOME_TIMEOUT_MILLIS = 10_000L

// The detail codes the intent ports answer with. The others come from the outcomes and the failures.
internal const val DETAIL_INVALID_ARGS = "invalid_args"
internal const val DETAIL_UNKNOWN_SUBJECT = "unknown_subject"
internal const val DETAIL_UNKNOWN_TERM = "unknown_term"
internal const val DETAIL_UNKNOWN_EVALUATION = "unknown_evaluation"
internal const val DETAIL_UNHANDLED = "unhandled"

// Runs one intent. Nothing it throws leaves the port: a failure is an answer, FAILED with the simple name of its class.
internal suspend fun guardIntent(block: suspend () -> IntentExecution): IntentExecution {
	return runCatching { block() }.getOrElse { failure -> failureDetailExecution(failure) }
}

// The answer to a failure of the intent itself. A cancellation is not an answer: it is passed on to the caller.
internal fun failureDetailExecution(failure: Throwable): IntentExecution {
	return IntentExecution(OutcomeKind.FAILED, failureDetailCode(failure))
}

// The detail code of a failure: the simple name of its class. A cancellation is never coded: it is passed on.
internal fun failureDetailCode(failure: Throwable): String {
	if (failure is CancellationException) throw failure

	return exceptionDetailCode(failure)
}

internal fun invalidArgs(): IntentExecution = IntentExecution(OutcomeKind.REJECTED, DETAIL_INVALID_ARGS)

internal fun unknownSubject(): IntentExecution = IntentExecution(OutcomeKind.REJECTED, DETAIL_UNKNOWN_SUBJECT)

internal fun unknownTerm(): IntentExecution = IntentExecution(OutcomeKind.REJECTED, DETAIL_UNKNOWN_TERM)

internal fun unknownEvaluation(): IntentExecution = IntentExecution(OutcomeKind.REJECTED, DETAIL_UNKNOWN_EVALUATION)

// The detail code of a use case error: the name of an enum entry, or the simple name of the error's class.
internal fun errorDetailCode(error: UseCaseError?): String {
	return when (error) {
		null -> DETAIL_UNHANDLED
		is Enum<*> -> error.name
		else -> error::class.simpleName ?: DETAIL_UNHANDLED
	}
}

internal fun exceptionDetailCode(failure: Throwable): String {
	return failure::class.simpleName ?: DETAIL_UNHANDLED
}
