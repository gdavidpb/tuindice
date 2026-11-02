package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantAttachmentRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.UploadAssistantAttachmentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.UploadAssistantAttachmentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.UploadAssistantAttachmentParams
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentAccepted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Sends a prepared attachment to the service, which keeps it for the student and answers with its id. The upload is
 * idempotent: the same bytes give back the attachment the service already keeps.
 */
class UploadAssistantAttachmentUseCase(
	private val attachmentRepository: AssistantAttachmentRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: UploadAssistantAttachmentExceptionHandler
) : FlowUseCase<UploadAssistantAttachmentParams, AttachmentAccepted, UploadAssistantAttachmentError>() {

	override suspend fun executeOnBackground(params: UploadAssistantAttachmentParams): Flow<AttachmentAccepted> = flow {
		val attachment = params.attachment

		emit(attachmentRepository.upload(content = attachment.content, name = attachment.name))
	}
}
