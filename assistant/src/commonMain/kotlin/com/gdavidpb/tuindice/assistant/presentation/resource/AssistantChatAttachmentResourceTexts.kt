package com.gdavidpb.tuindice.assistant.presentation.resource

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentFailure
import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_attachment_daily_limit
import tuindice.assistant.generated.resources.assistant_attachment_daily_limit_wait
import tuindice.assistant.generated.resources.assistant_attachment_expired
import tuindice.assistant.generated.resources.assistant_attachment_failed
import tuindice.assistant.generated.resources.assistant_attachment_limit
import tuindice.assistant.generated.resources.assistant_attachment_too_large
import tuindice.assistant.generated.resources.assistant_attachment_unsupported
import tuindice.assistant.generated.resources.assistant_attachments_refused

// The resource-backed attachment texts. The app binds them through AssistantChatResourceTexts, which carries them.
class AssistantChatAttachmentResourceTexts : AssistantChatAttachmentTexts {
	override suspend fun attachmentFailure(failure: AssistantAttachmentFailure): String {
		return when (failure) {
			AssistantAttachmentFailure.TooLarge -> getString(Res.string.assistant_attachment_too_large)

			AssistantAttachmentFailure.Unsupported -> getString(Res.string.assistant_attachment_unsupported)

			is AssistantAttachmentFailure.DailyLimit -> failure.retryAfterSeconds?.let { seconds ->
				getString(Res.string.assistant_attachment_daily_limit_wait, waitText(seconds))
			} ?: getString(Res.string.assistant_attachment_daily_limit)

			AssistantAttachmentFailure.Other -> getString(Res.string.assistant_attachment_failed)
		}
	}

	override suspend fun attachmentLimit(): String = getString(Res.string.assistant_attachment_limit)

	override suspend fun attachmentsRefused(): String = getString(Res.string.assistant_attachments_refused)

	override suspend fun attachmentExpired(): String = getString(Res.string.assistant_attachment_expired)
}
