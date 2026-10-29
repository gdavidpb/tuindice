package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.exception.AssistantDisabledException
import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantException
import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantExtractionRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantStandingRepository
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionRequest
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.MessageReport
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

// The assistant API the data layer calls. Each row of the contract is a function beside this class, grouped by
// resource in the Assistant*Api files, so the class stays small and every function keeps its contract path and JSON.
// The client is reached through [send] only: the functions beside the class never hold it.
class KtorAssistantApiDataSource(
	private val httpClient: HttpClient,
	private val serverDisabled: AssistantServerDisabledDataRepository
) : AssistantIntentRepository, AssistantFeedbackRepository, AssistantStandingRepository, AssistantExtractionRepository {
	override suspend fun decide(decision: IntentDecision): IntentDecisionResult = decideIntent(decision)

	override suspend fun reportOutcome(report: IntentOutcomeReport) = reportIntentOutcome(report)

	override suspend fun rate(feedback: MessageFeedback) = postFeedback(feedback)

	override suspend fun report(report: MessageReport) = postReport(report)

	override suspend fun status(): AssistantStatus = getStatus()

	override suspend fun appeal(appeal: AbuseAppeal) = postAbuseAppeal(appeal)

	override suspend fun extract(request: ExtractionRequest): ExtractionResult = postExtraction(request)

	// Runs one request. The client may fail an error answer by itself (expectSuccess): that answer is read from the
	// exception, so both kinds of failure reach the same mapping. The session of the request is read before it leaves:
	// a switch-off the answer carries is held for that session only.
	internal suspend fun send(call: suspend HttpClient.() -> HttpResponse): HttpResponse {
		val sessionMark = serverDisabled.sessionMark()
		val response = try {
			httpClient.call()
		} catch (cause: ResponseException) {
			cause.response
		}

		if (response.status.isSuccess()) return response

		val failure = response.toAssistantException()

		if (failure is AssistantDisabledException) {
			serverDisabled.markServerDisabled(failure.retryAfterSeconds, sessionMark)
		}

		throw failure
	}
}
