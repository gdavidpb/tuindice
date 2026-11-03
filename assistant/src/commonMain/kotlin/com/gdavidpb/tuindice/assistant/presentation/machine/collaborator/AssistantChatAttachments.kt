package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentFailure
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentState
import com.gdavidpb.tuindice.assistant.domain.model.PreparedAssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.usecase.PrepareAssistantAttachmentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.UploadAssistantAttachmentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.param.PrepareAssistantAttachmentParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.UploadAssistantAttachmentParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatDraft
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.isActive
import kotlin.uuid.Uuid

/**
 * The photos of the message being written. Each photo the student adds is read on the device and then sent to the
 * service, in one job of its own; the outcome of each step comes back to the machine as an event. The files are kept
 * until they are read, so a read that failed can start again, and an upload that failed sends what was read.
 */
class AssistantChatAttachments(
	private val prepareAssistantAttachmentUseCase: PrepareAssistantAttachmentUseCase,
	private val uploadAssistantAttachmentUseCase: UploadAssistantAttachmentUseCase
) {
	private val jobs = mutableMapOf<String, Job>()

	// The file of each photo not read yet. It leaves when the file is read, so a read that failed keeps it.
	private val files = mutableMapOf<String, PlatformFile>()

	// The digest of the bytes each photo read sends, by its id: the same picture is not sent twice.
	private val digestOf = mutableMapOf<String, String>()

	// Adds [picked] photos to the message as far as it has room. The photos past the room are not taken, and the chat
	// says so; each photo taken starts its own read and upload.
	internal fun add(
		host: MachineHost<AssistantChat.Effect>,
		draft: AssistantChatDraft,
		session: AssistantChatSession,
		picked: List<PlatformFile>
	) {
		val room = (AttachmentLimits.MAX_PER_MESSAGE - draft.attachments.value.size).coerceAtLeast(0)
		val taken = picked.take(room)

		session.attachmentLimitHit = picked.size > taken.size

		taken.forEach { file ->
			val localId = Uuid.random().toString()

			files[localId] = file
			draft.setAttachments(
				draft.attachments.value + AssistantAttachment(
					localId = localId,
					name = file.name,
					thumbnail = null,
					state = AssistantAttachmentState.Preparing,
					content = null
				)
			)

			start(host = host, draft = draft, localId = localId)
		}
	}

	// Tries the attachment [localId] again: one that could not be read starts over, one whose upload failed sends what was
	// read. Its tray item shows it as being read or sent again.
	fun retry(host: MachineHost<AssistantChat.Effect>, draft: AssistantChatDraft, localId: String) {
		val attachment = draft.attachments.value.firstOrNull { it.localId == localId } ?: return

		// Without what was read and without the file, there is nothing left to send: the attachment stays failed.
		if (attachment.content == null && files[localId] == null) return

		draft.updateAttachment(localId) { current ->
			current.copy(
				state = if (current.content == null) AssistantAttachmentState.Preparing else AssistantAttachmentState.Uploading
			)
		}

		start(host = host, draft = draft, localId = localId)
	}

	// Takes the attachment [localId] out: its read or upload stops, and its file is forgotten.
	fun remove(localId: String) {
		jobs.remove(localId)?.cancel()
		files.remove(localId)
		digestOf.remove(localId)
	}

	// The chat leaves its content: the reads and uploads stop, and each one in flight fails, so its retry is there on the
	// next opening. The files of the reads that did not finish are kept for that retry.
	fun stop(draft: AssistantChatDraft) {
		jobs.values.forEach { job -> job.cancel() }
		jobs.clear()

		draft.attachments.value
			.filter { attachment ->
				attachment.state == AssistantAttachmentState.Preparing || attachment.state == AssistantAttachmentState.Uploading
			}
			.forEach { attachment ->
				draft.updateAttachment(attachment.localId) { current ->
					current.copy(state = AssistantAttachmentState.Failed(AssistantAttachmentFailure.Other))
				}
			}
	}

	// One job per photo: its read, when it has none yet, and then its upload. Each outcome is reported as it comes.
	private fun start(host: MachineHost<AssistantChat.Effect>, draft: AssistantChatDraft, localId: String) {
		jobs[localId]?.cancel()

		jobs[localId] = host.launchMachineJob {
			val attachment = draft.attachments.value.firstOrNull { it.localId == localId } ?: return@launchMachineJob

			val prepared = attachment.content?.let { content ->
				PreparedAssistantAttachment(
					name = attachment.name,
					content = content,
					thumbnail = attachment.thumbnail
				)
			} ?: prepare(host = host, localId = localId)

			if (prepared == null) return@launchMachineJob

			// A file taken out meanwhile, or one the chat took out as the same picture as another, is not sent.
			if (!isActive || draft.attachments.value.none { held -> held.localId == localId }) {
				return@launchMachineJob
			}

			// The same picture as another photo of the message: the chat takes this one out (see the read's event), and
			// nothing is sent for it.
			if (holdsTheSamePicture(draft, localId, prepared.content.sha256)) return@launchMachineJob

			digestOf[localId] = prepared.content.sha256

			upload(host = host, localId = localId, prepared = prepared)
		}
	}

	// Another photo of the message holds the bytes this one reads to.
	private fun holdsTheSamePicture(draft: AssistantChatDraft, localId: String, digest: String): Boolean {
		return draft.attachments.value.any { held ->
			held.localId != localId && digestOf[held.localId] == digest
		}
	}

	// Reads the file of [localId] into what the upload sends. A file that cannot be read fails its attachment, and no
	// upload starts for it; the file stays, so the retry can read it again.
	private suspend fun prepare(
		host: MachineHost<AssistantChat.Effect>,
		localId: String
	): PreparedAssistantAttachment? {
		val file = files[localId] ?: return null

		val state = prepareAssistantAttachmentUseCase.execute(PrepareAssistantAttachmentParams(file = file)).last()

		return when (state) {
			is UseCaseState.Data -> {
				files.remove(localId)
				host.processInternalEvent(
					AssistantChatInternalEvent.AttachmentPrepared(localId, state.value)
				)
				state.value
			}

			is UseCaseState.Error -> {
				host.processInternalEvent(
					AssistantChatInternalEvent.AttachmentFailed(localId, state.error.toFailure())
				)
				null
			}

			is UseCaseState.Loading -> null
		}
	}

	// Sends what was read. The service keeps the photo, or refuses it for the reason the event says.
	private suspend fun upload(
		host: MachineHost<AssistantChat.Effect>,
		localId: String,
		prepared: PreparedAssistantAttachment
	) {
		val state = uploadAssistantAttachmentUseCase.execute(
			UploadAssistantAttachmentParams(attachment = prepared)
		).last()

		when (state) {
			is UseCaseState.Data -> host.processInternalEvent(
				AssistantChatInternalEvent.AttachmentUploaded(localId, state.value)
			)

			is UseCaseState.Error -> host.processInternalEvent(state.error.toUploadEvent(localId))

			is UseCaseState.Loading -> Unit
		}
	}
}
