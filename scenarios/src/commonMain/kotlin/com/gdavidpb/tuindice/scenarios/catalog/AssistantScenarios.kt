package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.mockState
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within

/** Taps the assistant's sparkle in the top bar and waits for the chat sheet. */
private fun StepBuilder.openAssistant() {
	tap(BaseUiTags.topBarActionButton(TopBarAction.AssistantAction))
	waitVisible(AssistantUiTags.ChatSheet, Within.Wait)
}

/** Accepts the consent the sheet asks for at its first opening, then waits for the composer. */
private fun StepBuilder.acceptConsent() {
	waitVisible(AssistantUiTags.ConsentView, Within.Wait)
	tap(AssistantUiTags.ConsentAccept)
	waitVisible(AssistantUiTags.Composer, Within.Action)
}

/** Writes [message] in the composer and sends it. */
private fun StepBuilder.sendText(message: String) {
	enterText(AssistantUiTags.Composer, message)
	tap(AssistantUiTags.SendButton)
}

/** Waits until the agent message [messageId] is on the screen. */
private fun StepBuilder.waitAgentMessage(messageId: String) {
	waitVisible(AssistantUiTags.Message.agent(messageId), Within.Action)
}

private val assistantSmoke = scenario(
	"assistant-smoke",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	tags("smoke")
	covers(
		"assistant.AssistantChat.AcceptConsent",
		"assistant.AssistantChat.PickSuggestion",
		"assistant.AssistantChat.ChangeText",
		"assistant.AssistantChat.Send",
		"assistant.AssistantChat.CopyMessage",
		"assistant.AssistantChat.RateMessage"
	)
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	tap(AssistantUiTags.Chat.suggestion("sq.summary.term"))
	waitAgentMessage("msg-suggestion")
	sendText("¿Y mis evaluaciones?")
	waitAgentMessage("msg-followup")
	tap(AssistantUiTags.Message.copy("msg-followup"))
	waitVisible(BaseUiTags.SnackbarContainer, Within.Action)
	tap(AssistantUiTags.Message.useful("msg-followup"))
}

private val assistantConfirmAddEvaluation = scenario(
	"assistant-confirm-add-evaluation",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, section = MainSection.EVALUATIONS).toLaunchSpec()
) {
	covers(
		"assistant.AssistantChat.ToggleIntentDraft",
		"assistant.AssistantChat.ConfirmIntent",
		"assistant.AssistantChat.OpenNavTarget"
	)
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Agrega mi evaluación")
	waitVisible(AssistantUiTags.Intent.card("intent-add-1"), Within.Action)
	tap(AssistantUiTags.Intent.draft("intent-add-1", "mut-add-2"))
	tap(AssistantUiTags.Intent.confirm("intent-add-1"))
	waitVisible(AssistantUiTags.Intent.receipt("intent-add-1"), Within.Action)
	tap(AssistantUiTags.Component.openScreen("cmp-add-nav-1"))
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
	waitVisible(text(Copy.EvaluationQuizName), Within.Action)
}

private val assistantConfirmDestructiveDelete = scenario(
	"assistant-confirm-destructive-delete",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, section = MainSection.EVALUATIONS).toLaunchSpec()
) {
	covers("assistant.AssistantChat.ConfirmIntent")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Borra la evaluación")
	waitVisible(AssistantUiTags.Intent.card("intent-delete-1"), Within.Action)
	tap(AssistantUiTags.Intent.confirm("intent-delete-1"))
	waitVisible(AssistantUiTags.Intent.receipt("intent-delete-1"), Within.Action)
	tap(AssistantUiTags.Component.openScreen("cmp-delete-nav-1"))
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
	waitGone(EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value), Within.Action)
}

private val assistantDismissIntent = scenario(
	"assistant-dismiss-intent",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, section = MainSection.EVALUATIONS).toLaunchSpec()
) {
	covers("assistant.AssistantChat.DismissIntent")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Agrega mi evaluación")
	waitVisible(AssistantUiTags.Intent.card("intent-add-1"), Within.Action)
	tap(AssistantUiTags.Intent.cancel("intent-add-1"))
	waitGone(AssistantUiTags.Intent.card("intent-add-1"), Within.Action)
}

private val assistantAskAboutEvaluation = scenario(
	"assistant-ask-about-evaluation",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, section = MainSection.EVALUATIONS).toLaunchSpec()
) {
	covers("assistant.AssistantChat.RemoveContext")
	account(E2eAccounts.Canonical.id)

	val card = EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value)
	scrollUntilVisible(card, Scroll.ContentDown, Within.Action)
	tap(card)
	waitVisible(EvaluationsUiTags.EvaluationSwipeToDismissContainer, Within.Assert)
	tap(EvaluationsUiTags.EvaluationSwipeAskAction)
	acceptConsent()
	waitVisible(AssistantUiTags.ContextChip, Within.Action)
	sendText("¿Cuánto necesito?")
	waitAgentMessage("msg-ask")
	tap(AssistantUiTags.ContextRemove)
	waitGone(AssistantUiTags.ContextChip, Within.Assert)
}

private val assistantServerDisabled = scenario(
	"assistant-server-disabled",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	mockState("assistant-turns", "Disabled")
	sendText("Hola sin servicio")
	waitGone(AssistantUiTags.ChatSheet, Within.Action)
	waitGone(BaseUiTags.topBarActionButton(TopBarAction.AssistantAction), Within.Assert)
}

private val assistantGuidedSuggestions = scenario(
	"assistant-guided-suggestions",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantChat.PickSuggestion")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	mockState("assistant-turns", "Guided")
	sendText("Hola guiado")
	waitVisible(AssistantUiTags.Notice, Within.Action)
	assertEnabled(AssistantUiTags.Composer, false)
	tap(AssistantUiTags.Chat.suggestion("sq.summary.term"))
	waitAgentMessage("msg-suggestion")
}

private val assistantDeclineConsent = scenario(
	"assistant-decline-consent",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantChat.DeclineConsent")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	waitVisible(AssistantUiTags.ConsentView, Within.Wait)
	tap(AssistantUiTags.ConsentDecline)
	waitGone(AssistantUiTags.ChatSheet, Within.Action)
}

private val assistantRetryTurn = scenario(
	"assistant-retry-turn",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantChat.RetryTurn")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Falla una vez")
	waitVisible(AssistantUiTags.NoticeTurnFailed, Within.Action)
	tap(AssistantUiTags.NoticeRetry)
	waitAgentMessage("msg-retry")
}

private val assistantReportMessage = scenario(
	"assistant-report-message",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantChat.ReportMessage")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Dame un dato")
	waitAgentMessage("msg-report")
	tap(AssistantUiTags.Message.report("msg-report"))
	tap(text(Copy.AssistantReportIncorrect))
	waitGone(text(Copy.AssistantReportIncorrect), Within.Action)
}

private val assistantAnswerStillApplies = scenario(
	"assistant-answer-still-applies",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantChat.AnswerStillApplies")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Recuerda mi promedio")
	waitVisible(AssistantUiTags.Memory.stillAppliesYes("mem-1"), Within.Action)
	tap(AssistantUiTags.Memory.stillAppliesYes("mem-1"))
	waitGone(AssistantUiTags.Memory.stillAppliesYes("mem-1"), Within.Action)
}

private val assistantOpenComponentSheet = scenario(
	"assistant-open-component-sheet",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"assistant.AssistantChat.OpenComponentSheet",
		"assistant.AssistantComponentSheet.OpenNavTarget"
	)
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Muestra mi trimestre")
	waitVisible(AssistantUiTags.Component.root("cmp-sheet-nav-1"), Within.Action)
	tap(AssistantUiTags.Component.root("cmp-sheet-nav-1"))
	tap(AssistantUiTags.Component.openScreen("cmp-sheet-nav-1"))
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
}

private val assistantOpenScreen = scenario(
	"assistant-open-screen",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantChat.OpenNavTarget")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Llévame a evaluaciones")
	waitVisible(AssistantUiTags.Component.openScreen("cmp-nav-1"), Within.Action)
	tap(AssistantUiTags.Component.openScreen("cmp-nav-1"))
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
}

/** The scenarios of the `assistant` module; list every new one here. */
val assistantScenarios: List<Scenario> = listOf(
	assistantSmoke,
	assistantConfirmAddEvaluation,
	assistantConfirmDestructiveDelete,
	assistantDismissIntent,
	assistantAskAboutEvaluation,
	assistantServerDisabled,
	assistantGuidedSuggestions,
	assistantDeclineConsent,
	assistantRetryTurn,
	assistantReportMessage,
	assistantAnswerStillApplies,
	assistantOpenComponentSheet,
	assistantOpenScreen
)
