package com.gdavidpb.tuindice.assistant.presentation.contract

import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatContextItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason
import io.github.vinceglb.filekit.PlatformFile

// The chat sheet. It carries no title, name or icon of the assistant: inside the chat the assistant is unnamed.
object AssistantChat {
	sealed class State : ViewState {
		data object Idle : State()

		// The student has not accepted the assistant's consent: the sheet asks for it before anything else.
		data object ConsentRequired : State()

		// The consent could not be given: the sheet asks for it again, and says why.
		data object ConsentFailed : State()

		// The assistant went off while the sheet was open: the sheet closes with Effect.Dismiss.
		data object Unavailable : State()

		data class Content(
			// The screen the sheet was opened from, as a chip the student can remove. Null once removed.
			val context: ChatContextItem?,
			// The conversation, oldest first: the student's bubbles and the agent's messages.
			val items: List<ChatItem>,
			// The impersonal label of the running turn («Revisando tu pensum…»), shown with the atom; null when idle.
			val progressLabel: String?,
			val composer: ChatComposerItem,
			val suggestions: List<ChatSuggestionItem>,
			// What stops or limits the chat right now (a failed turn, the quota, a restriction, guided mode).
			val notice: ChatNoticeItem?,
			// Guided mode: the composer is off and only the suggestions can start a turn.
			val isGuided: Boolean,
			// A photo of the plan is being read: its progress label shows, and the photo button is off.
			val isExtracting: Boolean = false,
			// A message was sent and its first event has not come yet: the progress shows, and nothing can be sent.
			val isSending: Boolean = false
		) : State()
	}

	sealed class Action : ViewAction {
		// The sheet opened from a screen; [context] is what that screen declared, or null.
		class Open(val context: AssistantScreenContext?) : Action()
		class ChangeText(val text: String) : Action()
		data object Send : Action()
		class PickSuggestion(val suggestionId: String) : Action()
		data object RemoveContext : Action()

		// Opens the camera, or the gallery, to read the evaluations of a photo of the plan.
		class RequestPhoto(val fromCamera: Boolean) : Action()

		// The photo the student took or picked: it is read into the conversation as a proposal.
		class AttachPhoto(val file: PlatformFile) : Action()

		// Sends again the student's message whose turn failed.
		data object RetryTurn : Action()

		// Stops the turn that runs, or the one being sent: the server cancels it, and its bubble says so.
		data object CancelTurn : Action()

		// Confirms the intent [intentId] from its own card. A destructive intent has no other way to be confirmed.
		class ConfirmIntent(val intentId: String) : Action()
		class DismissIntent(val intentId: String) : Action()

		// Marks or unmarks one row of a batch intent (evaluaciones.agregar) before confirming it.
		class ToggleIntentDraft(val intentId: String, val mutationId: String, val selected: Boolean) : Action()

		class CopyMessage(val messageId: String) : Action()
		class RateMessage(val messageId: String, val useful: Boolean) : Action()
		class ReportMessage(val messageId: String, val reason: ReportReason, val comment: String?) : Action()

		// A nav.abrir component or a link inside a component was tapped.
		class OpenNavTarget(val target: NavTarget, val entityRefs: List<EntityRef>) : Action()

		// A component shown as SHEET was tapped: it opens on its own sheet.
		class OpenComponentSheet(val messageId: String, val componentId: String) : Action()

		class AnswerStillApplies(val memoryId: String, val applies: Boolean) : Action()
		class Appeal(val text: String) : Action()
		data object AcceptConsent : Action()
		data object DeclineConsent : Action()

		// The privacy policy link of the consent: the same policy the rest of the app opens.
		data object OpenPrivacyPolicy : Action()
		data object Dismiss : Action()
	}

	sealed class Effect : ViewEffect {
		data object Dismiss : Effect()
		class CopyText(val text: String) : Effect()
		class ShowSnackBar(val message: String) : Effect()
		class NavigateTo(val target: NavTarget, val entityRefs: List<EntityRef>) : Effect()

		// A page outside the app (the privacy policy), opened by the system as the chat's support links are: the sheet
		// stays as it was underneath, and coming back to the app finds it there.
		class OpenUrl(val url: String) : Effect()

		// comprobante.pedir: the app's own enrollment proof flow takes over, with the password saved on the device.
		data object OpenEnrollmentProof : Effect()
		class OpenComponentSheet(val messageId: String, val componentId: String) : Effect()

		// The route opens the camera, or the gallery of the system, and answers with AttachPhoto.
		data object OpenCamera : Effect()

		data object OpenPhotoPicker : Effect()
	}
}
