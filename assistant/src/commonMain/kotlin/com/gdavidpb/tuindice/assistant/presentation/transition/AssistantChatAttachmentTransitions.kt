package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentFailure
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentState
import com.gdavidpb.tuindice.assistant.domain.usecase.error.UploadAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits

private typealias AttachmentTransitions = MachineDefinitionBuilder.StateTransitions<
	AssistantChat.State,
	AssistantChat.State.Content
>

// The attachments of the message being written: the clip, the tray's actions and what the reads and uploads answer.
internal fun AttachmentTransitions.contentAttachmentRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	// The clip asks for a source: the camera, or the photos of the gallery. A picker takes at most the room the message
	// has left. With no room left the clip is off, and the notice says why (see presentedNotice).
	on<AssistantChat.Action.RequestAttachment>(
		emits = setOf(
			AssistantChat.Effect.OpenCamera::class,
			AssistantChat.Effect.OpenPhotoPicker::class
		)
	) { state, action ->
		if (!state.composer.isAttachEnabled) return@on state

		val room = AttachmentLimits.MAX_PER_MESSAGE - machine.draft.attachments.value.size

		host.sendEffect(
			when (action.source) {
				AssistantChat.AttachmentSource.CAMERA -> AssistantChat.Effect.OpenCamera
				AssistantChat.AttachmentSource.GALLERY -> AssistantChat.Effect.OpenPhotoPicker(maxItems = room)
			}
		)

		state
	}

	on<AssistantChat.Action.AddAttachments> { state, action ->
		if (!state.composer.isAttachEnabled) return@on state

		machine.turns.attachments.add(
			host = host,
			draft = machine.draft,
			session = machine.session,
			picked = action.files
		)
		machine.refreshed(state)
	}

	on<AssistantChat.Action.RemoveAttachment> { state, action ->
		machine.turns.attachments.remove(localId = action.localId)
		machine.draft.removeAttachment(action.localId)
		machine.session.attachmentLimitHit = false
		machine.refreshed(state)
	}

	// Only a failed attachment is tried again, and only while the composer is open.
	on<AssistantChat.Action.RetryAttachment> { state, action ->
		val attachment = machine.draft.attachments.value.firstOrNull { it.localId == action.localId }

		if (!state.composer.isEnabled || attachment?.state !is AssistantAttachmentState.Failed) return@on state

		machine.turns.attachments.retry(host = host, draft = machine.draft, localId = action.localId)
		machine.refreshed(state)
	}
}

// The outcomes of the attachments' reads and uploads: each one shows in its tray item, and a refusal the chat answers
// keeps its notice.
internal fun AttachmentTransitions.contentAttachmentResultRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	// The read of a file is done: its upload is on its way, and the tray shows it as sent.
	// A file read to the bytes of an attachment the message already holds is the same picture: the one already there is
	// kept, and this one leaves the tray with its read and its upload.
	on<AssistantChatInternalEvent.AttachmentPrepared> { state, event ->
		// A photo taken out while it was read is not kept, and nothing is sent for it.
		if (machine.draft.attachments.value.none { attachment -> attachment.localId == event.localId }) {
			return@on state
		}

		val sameBytes = machine.draft.attachments.value.any { other ->
			other.localId != event.localId && other.content?.sha256 == event.prepared.content.sha256
		}

		if (sameBytes) {
			machine.turns.attachments.remove(localId = event.localId)
			machine.draft.removeAttachment(event.localId)
			// The copy leaves the tray, so the tray has room again: the limit the picks reached is not said any more.
			machine.session.attachmentLimitHit = false
			return@on machine.refreshed(state)
		}

		// The read is over and its bytes are with the attachment: the file is not held for a retry any more.
		machine.turns.attachments.readFinished(localId = event.localId)
		machine.draft.updateAttachment(event.localId) { attachment ->
			attachment.copy(
				name = event.prepared.name,
				thumbnail = event.prepared.thumbnail,
				state = AssistantAttachmentState.Uploading,
				content = event.prepared.content
			)
		}
		machine.turns.attachments.startUpload(host = host, localId = event.localId, prepared = event.prepared)
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.AttachmentUploaded> { state, event ->
		machine.draft.updateAttachment(event.localId) { attachment ->
			attachment.copy(state = AssistantAttachmentState.Ready(event.accepted.attachmentId))
		}
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.AttachmentFailed> { state, event ->
		machine.draft.updateAttachment(event.localId) { attachment ->
			attachment.copy(state = AssistantAttachmentState.Failed(event.reason))
		}
		machine.refreshed(state)
	}

	on<AssistantChatInternalEvent.AttachmentRejected> { state, event ->
		machine.draft.updateAttachment(event.localId) { attachment ->
			attachment.copy(state = AssistantAttachmentState.Failed(AssistantAttachmentFailure.Other))
		}
		machine.attachmentRejected(host = host, error = event.error)
		machine.refreshed(state)
	}
}

// An upload the service refused for a reason the whole chat answers: a quota is the notice of the chat until its wait
// passes; a restriction and the guided mode are read again from the server, as a refused text send reads them.
private suspend fun AssistantChatMachine.attachmentRejected(
	host: MachineHost<AssistantChat.Effect>,
	error: UploadAssistantAttachmentError
) {
	when (error) {
		is UploadAssistantAttachmentError.QuotaExhausted -> {
			session.quota = AssistantChatSession.Quota(retryAfterSeconds = error.retryAfterSeconds)
		}

		UploadAssistantAttachmentError.Restricted -> {
			session.restriction = AssistantChatSession.Restriction(level = null, untilMillis = null, canAppeal = false)
			access.restrictions.restrictionStatus(host)
		}

		UploadAssistantAttachmentError.GuidedOnly -> {
			session.guidedByServer = true
			access.refreshStatus(host)
		}

		else -> Unit
	}
}
