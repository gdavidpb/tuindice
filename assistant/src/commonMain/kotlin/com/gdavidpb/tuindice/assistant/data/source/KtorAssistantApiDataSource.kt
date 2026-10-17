package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantFeedbackRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantStandingRepository
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.MessageReport
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus
import io.ktor.client.HttpClient

// The assistant API the data layer calls. Each row of the contract is a function beside this class, grouped by
// resource in the Assistant*Api files, so the class stays small and every function keeps its contract path and JSON.
class KtorAssistantApiDataSource(
	internal val httpClient: HttpClient,
	internal val serverDisabled: AssistantServerDisabledDataRepository
) : AssistantIntentRepository, AssistantFeedbackRepository, AssistantStandingRepository {
	override suspend fun decide(decision: IntentDecision): IntentDecisionResult = decideIntent(decision)

	override suspend fun reportOutcome(report: IntentOutcomeReport) = postIntentOutcome(report.intentId, report)

	override suspend fun rate(feedback: MessageFeedback) = postFeedback(feedback)

	override suspend fun report(report: MessageReport) = postReport(report)

	override suspend fun status(): AssistantStatus = getStatus()

	override suspend fun appeal(appeal: AbuseAppeal) = postAbuseAppeal(appeal)
}
