package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentFailure
import com.gdavidpb.tuindice.assistant.domain.usecase.error.PrepareAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.domain.usecase.error.UploadAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent

// The outcomes of the reads and the uploads, as the chat shows them: each refusal of a read or an upload, as the tray
// or the chat tells it.

internal fun PrepareAssistantAttachmentError?.toFailure(): AssistantAttachmentFailure {
	return when (this) {
		PrepareAssistantAttachmentError.TooLarge -> AssistantAttachmentFailure.TooLarge

		PrepareAssistantAttachmentError.Unreadable -> AssistantAttachmentFailure.Unsupported

		null -> AssistantAttachmentFailure.Other
	}
}

// The refusals the whole chat answers keep their own events: the disabled service closes the chat, a missing consent
// asks for it, and a quota, a restriction or the guided mode are told as the chat tells them. The others fail the
// attachment.
internal fun UploadAssistantAttachmentError?.toUploadEvent(localId: String): AssistantChatInternalEvent {
	return when (this) {
		is UploadAssistantAttachmentError.AssistantDisabled -> AssistantChatInternalEvent.SendDisabled

		UploadAssistantAttachmentError.ConsentRequired -> AssistantChatInternalEvent.SendConsentRequired

		is UploadAssistantAttachmentError.QuotaExhausted,
		UploadAssistantAttachmentError.Restricted,
		UploadAssistantAttachmentError.GuidedOnly -> AssistantChatInternalEvent.AttachmentRejected(
			localId = localId,
			error = this
		)

		UploadAssistantAttachmentError.TooLarge ->
			AssistantChatInternalEvent.AttachmentFailed(localId, AssistantAttachmentFailure.TooLarge)

		UploadAssistantAttachmentError.Unsupported ->
			AssistantChatInternalEvent.AttachmentFailed(localId, AssistantAttachmentFailure.Unsupported)

		is UploadAssistantAttachmentError.DailyLimit ->
			AssistantChatInternalEvent.AttachmentFailed(localId, AssistantAttachmentFailure.DailyLimit(retryAfterSeconds))

		UploadAssistantAttachmentError.Other, null ->
			AssistantChatInternalEvent.AttachmentFailed(localId, AssistantAttachmentFailure.Other)
	}
}
