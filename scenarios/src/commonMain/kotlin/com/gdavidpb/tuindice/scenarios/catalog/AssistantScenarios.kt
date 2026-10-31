package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.expectRequest
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.mockState
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.swipePoints
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitBackgrounded
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.showGradedEvaluation
import com.gdavidpb.tuindice.scenarios.shared.showWeekOfGradedEvaluation

// The sheet fills the available window, so the 8 % point lands on its drag handle rather than a tappable scrim.
// Drag down from that handle to dismiss it.
private const val CHAT_SHEET_HANDLE_X = 0.5
private const val CHAT_SHEET_HANDLE_Y = 0.08
private const val CHAT_SHEET_DISMISS_DRAG_Y = 0.6

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
	waitVisible(AssistantUiTags.ChatSnackbar, Within.Action)
	waitVisible(text(Copy.AssistantCopied), Within.Assert)
	// The snackbar sits above the composer, over the last message's actions while the keyboard is open: the thumb is
	// tapped once it has gone, as a person would after reading it.
	waitGone(AssistantUiTags.ChatSnackbar, Within.Long)
	tap(AssistantUiTags.Message.useful("msg-followup"))
	expectRequest("POST", "/assistant/v1/feedback", status = 204)
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
	waitVisible(text(Copy.AssistantReceiptAddedOne), Within.Assert)
	tap(AssistantUiTags.Component.openScreen("cmp-add-nav-1"))
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
	// The added row is on the list with the grade the proposal gave it.
	waitVisible(text(E2eFixtures.AddedPartialOneGrade.value), Within.Action)
}

private val assistantConfirmDestructiveDelete = scenario(
	"assistant-confirm-destructive-delete",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, section = MainSection.EVALUATIONS).toLaunchSpec()
) {
	covers("assistant.AssistantChat.ConfirmIntent")
	account(E2eAccounts.Canonical.id)

	showGradedEvaluation()
	scrollUntilVisible(
		EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value),
		Scroll.ContentDown,
		Within.Action
	)
	openAssistant()
	acceptConsent()
	sendText("Borra la evaluación")
	waitVisible(AssistantUiTags.Intent.card("intent-delete-1"), Within.Action)
	tap(AssistantUiTags.Intent.confirm("intent-delete-1"))
	waitVisible(AssistantUiTags.Intent.receipt("intent-delete-1"), Within.Action)
	tap(AssistantUiTags.Component.openScreen("cmp-delete-nav-1"))
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
	showWeekOfGradedEvaluation()
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
	// The card stays with its outcome: a cancelled proposal reads «Descartado», so the student sees what became of it.
	waitVisible(text(Copy.AssistantIntentCancelled), Within.Action)
	waitGone(AssistantUiTags.Intent.cancel("intent-add-1"), Within.Assert)
}

private val assistantAskAboutEvaluation = scenario(
	"assistant-ask-about-evaluation",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, section = MainSection.EVALUATIONS).toLaunchSpec()
) {
	covers("assistant.AssistantChat.RemoveContext")
	account(E2eAccounts.Canonical.id)

	val card = EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value)
	showGradedEvaluation()
	scrollUntilVisible(card, Scroll.ContentDown, Within.Action)
	tap(card)
	waitVisible(EvaluationsUiTags.EvaluationSwipeToDismissContainer, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationSwipeAskAction, Within.Action)
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
	waitGone(AssistantUiTags.Composer, Within.Assert)
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

private val assistantConsentPrivacyPolicy = scenario(
	"assistant-consent-privacy-policy",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantChat.OpenPrivacyPolicy")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	waitVisible(AssistantUiTags.ConsentView, Within.Wait)
	// The policy opens outside the app; coming back finds the consent as it was.
	tap(AssistantUiTags.ConsentPrivacy)
	waitBackgrounded(Within.Action)
	foreground()
	waitVisible(AssistantUiTags.ConsentView, Within.Action)
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
	expectRequest("POST", "/assistant/v1/reports", status = 204)
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
	waitVisible(AssistantUiTags.Memory.stillAppliesYes("mem-2"), Within.Action)
	tap(AssistantUiTags.Memory.stillAppliesYes("mem-2"))
	waitGone(AssistantUiTags.Memory.stillAppliesYes("mem-2"), Within.Action)
	// The question keeps the answer the student gave.
	waitVisible(text(Copy.AssistantStillAppliesYes), Within.Action)
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

// The evaluations list draws each evaluation the device has with the Evaluations tab's own card. The service calls
// the written work of CI5437 pending and titles it «Trabajo escrito»; its date has passed with no grade, so the tab
// writes it «TRABAJO ESCRITO 1» and «Sin nota», and the chat card does the same rather than «Programada». The scenario
// starts on the Evaluations tab, so the device has its evaluations before the turn; the tab opens on the last week,
// which holds neither that name nor a scheduled evaluation.
private val assistantEvaluationsCard = scenario(
	"assistant-evaluations-card",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, section = MainSection.EVALUATIONS).toLaunchSpec()
) {
	account(E2eAccounts.Canonical.id)

	val overdue = E2eFixtures.GradedEvaluation.value
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Sync)
	waitVisible(EvaluationsUiTags.EvaluationsWeekStrip, Within.Long)
	openAssistant()
	acceptConsent()
	sendText("Muestra mis evaluaciones de CI5437")
	waitAgentMessage("msg-evaluations")
	// The list's second row, the completed model, is not asked for: with the keyboard open it lies under the composer
	// on the smaller screen.
	waitVisible(AssistantUiTags.Evaluations.row("cmp-evaluations-1", overdue), Within.Action)
	waitVisible(text(E2eFixtures.GradedEvaluationName.value), Within.Assert)
	waitVisible(text(Copy.EvaluationNoGrade), Within.Assert)
	waitGone(text(Copy.AssistantEvaluationPending), Within.Assert)
	// The tab, on the week of that evaluation, shows it the same way.
	swipePoints(CHAT_SHEET_HANDLE_X, CHAT_SHEET_HANDLE_Y, 0.0, CHAT_SHEET_DISMISS_DRAG_Y)
	waitGone(AssistantUiTags.ChatSheet, Within.Action)
	showWeekOfGradedEvaluation()
	waitVisible(EvaluationsUiTags.evaluationItemCard(overdue), Within.Action)
	waitVisible(text(E2eFixtures.GradedEvaluationName.value), Within.Assert)
	waitVisible(text(Copy.EvaluationNoGrade), Within.Assert)
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

private val assistantReopenWithConsent = scenario(
	"assistant-reopen-with-consent",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantChat.Dismiss")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	// Drag down from the handle to close the sheet. Consent is given, so the sparkle
	// reopens straight into the conversation with the composer in view.
	swipePoints(CHAT_SHEET_HANDLE_X, CHAT_SHEET_HANDLE_Y, 0.0, CHAT_SHEET_DISMISS_DRAG_Y)
	waitGone(AssistantUiTags.ChatSheet, Within.Action)
	openAssistant()
	waitVisible(AssistantUiTags.Composer, Within.Action)
}

private val assistantAskAboutRecordSubject = scenario(
	"assistant-ask-about-record-subject",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, section = MainSection.RECORD).toLaunchSpec()
) {
	account(E2eAccounts.Canonical.id)

	// A subject's card in the record offers to ask about it, and the chat opens with that subject as its context.
	val card = RecordUiTags.attemptCard(E2eFixtures.SyntheticEp1308Attempt.value)
	waitVisible(card, Within.Sync)
	tap(card)
	tap(BaseUiTags.askAssistant("menu_item"))
	acceptConsent()
	waitVisible(AssistantUiTags.ContextChip, Within.Action)
}

// The answer of this send waits in its stream, so the stop button stays on the composer until the student stops
// the turn. The send and its stream are in a state of their own, which only this scenario starts in.
private val assistantCancelTurn = scenario(
	"assistant-cancel-turn",
	"assistant",
	Start.Seeded(E2eAccounts.Canonical, mockStates = listOf(MockState("assistant-turn-pace", "Slow"))).toLaunchSpec()
) {
	covers("assistant.AssistantChat.CancelTurn")
	account(E2eAccounts.Canonical.id)

	openAssistant()
	acceptConsent()
	sendText("Piensa con calma")
	waitVisible(AssistantUiTags.StopButton, Within.Action)
	tap(AssistantUiTags.StopButton)
	waitGone(AssistantUiTags.StopButton, Within.Action)
	waitVisible(AssistantUiTags.SendButton, Within.Action)
}

// The suspension of the account can be appealed: its status says so from the state of this scenario, and the notice of
// the restriction offers «Apelar». The appeal goes to the service with the text the student writes, and the service
// answers it with the snackbar of the sent appeal.
private val assistantAppealRestriction = scenario(
	"assistant-appeal-restriction",
	"assistant",
	Start.Seeded(
		E2eAccounts.Canonical,
		mockStates = listOf(MockState("assistant-restriction", "Restricted"))
	).toLaunchSpec()
) {
	covers("assistant.AssistantChat.Appeal")
	account(E2eAccounts.Canonical.id)

	// In the Restricted state, /status reports the consent accepted: the chat opens on the restriction notice.
	openAssistant()
	waitVisible(AssistantUiTags.Notice, Within.Action)
	tap(AssistantUiTags.AppealAction)
	enterText(AssistantUiTags.AppealText, "Fue un error de mi parte")
	tap(AssistantUiTags.AppealSend)
	waitVisible(AssistantUiTags.ChatSnackbar, Within.Action)
	waitVisible(text(Copy.AssistantAppealSent), Within.Assert)
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
	assistantConsentPrivacyPolicy,
	assistantRetryTurn,
	assistantReportMessage,
	assistantAnswerStillApplies,
	assistantOpenComponentSheet,
	assistantEvaluationsCard,
	assistantOpenScreen,
	assistantReopenWithConsent,
	assistantAskAboutRecordSubject,
	assistantCancelTurn,
	assistantAppealRestriction
)
